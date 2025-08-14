package com.example.cms.contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.cms.entity.*;
import com.example.cms.service.*;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CmsController {

    // LOGIN FLAGS & TRACKERS
    private boolean adminLogFlag = false;
    private boolean studentLogFlag = false;
    private boolean counselorLogFlag = false;
    private long currentUserId = 0; // Tracks logged-in user ID (studentId or counselorId)

    // SERVICES
    @Autowired
    private CMSUsersService cus;
    @Autowired
    private MappingDetailsService mds;
    @Autowired
    private ScheduledAppointmentsService sas;
    @Autowired
    private CMSContactService ccs;
    @Autowired
    private MeetingLinkService mls;

    // ---------------- BASIC ROUTES ---------------- //

    @GetMapping("/")
    public String home() {
        return "Home1";
    }

    @PostMapping("/contact")
    public String contactForm(@ModelAttribute CMSContact cc) {
        ccs.addToDB(cc);
        return "redirect:/";
    }

    // ---------------- LOGIN / REGISTER ---------------- //

    @GetMapping("/login")
    public String loginForm() {
        return "Login_form";
    }

    @PostMapping("/user")
    public String roleBasedLogin(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        long id = Long.parseLong(request.getParameter("id"));
        String password = request.getParameter("password");
        int idLen = String.valueOf(id).length();

        if (id == 1234 && password.equals("1234")) { // Admin
            adminLogFlag = true;
            currentUserId = id;
            redirectAttributes.addFlashAttribute("successMessage", "Welcome Admin: " + id + " 👮‍♂️");
            return "redirect:/admin";
        }

        List<CMSUsers> users = cus.getAllUsers();
        for (CMSUsers user : users) {
            if (user.getId() == id) {
                if (idLen == 4 && user.getPassword().equals(password)) { // Counselor
                    counselorLogFlag = true;
                    currentUserId = id; // Storing counselorId as long but will cast to int where needed
                    redirectAttributes.addFlashAttribute("successMessage", "Welcome Counselor: " + id + " 👨‍✈️");
                    return "redirect:/counselor";
                }
                if (idLen == 10 && user.getPassword().equals(password)) { // Student
                    studentLogFlag = true;
                    currentUserId = id;
                    redirectAttributes.addFlashAttribute("successMessage",
                            mds.checkUser(id)
                                    ? "Welcome Student: " + id + " 👨‍🎓"
                                    : "Welcome Student: " + id + "\nYour counseling details are not yet available.");
                    return "redirect:/student";
                }
                redirectAttributes.addFlashAttribute("successMessage", "Wrong password for: " + id);
                return "redirect:/register";
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "User Not Found ❌ Please Register First.");
        return "redirect:/register";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "Register_form";
    }

    @PostMapping("/save")
    public String registerUsers(@ModelAttribute CMSUsers cu, RedirectAttributes redirectAttributes) {
        if (cus.getByID(cu.getId())) {
            redirectAttributes.addFlashAttribute("successMessage", "User Already Exists ✔ Please log in.");
            return "redirect:/register";
        }
        cus.storeToDB(cu);
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful ✔ Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        adminLogFlag = false;
        studentLogFlag = false;
        counselorLogFlag = false;
        currentUserId = 0;
        return "redirect:/login";
    }

    @GetMapping("/error")
    public String errorPage() {
        return "Error";
    }

    // ---------------- STUDENT PANEL ---------------- //

    @GetMapping("/student")
    public String studentDashboard() {
        return studentLogFlag ? "Student" : "redirect:/";
    }

    @GetMapping("/myCounselor")
    public ModelAndView getMyCounselor(RedirectAttributes redirectAttributes) {
        if (!studentLogFlag) return new ModelAndView("Error");

        MappingDetails matchObj = mds.getById(currentUserId);
        if (matchObj == null) {
            redirectAttributes.addFlashAttribute("successMessage", "No counselor data found for your account.");
            return new ModelAndView("Student");
        }
        return new ModelAndView("viewMyCounselor", "mc", matchObj);
    }

    @GetMapping("/Appointment")
    public String scheduleForm() {
        return studentLogFlag ? "ScheduleAppointment" : "redirect:/error";
    }

    @GetMapping("/yourChannel")
    public ModelAndView getYourChannel(RedirectAttributes redirectAttributes) {
        if (!studentLogFlag) return new ModelAndView("Error");

        MappingDetails matchObj = mds.getById(currentUserId);
        if (matchObj == null) {
            redirectAttributes.addFlashAttribute("successMessage", "No channel data found for your account.");
            return new ModelAndView("Student");
        }
        return new ModelAndView("telegramChannel", "tc", matchObj);
    }

    @PostMapping("/addschedule")
    public String addNewAppointment(@ModelAttribute ScheduleAppointment sa, RedirectAttributes redirectAttributes) {
        if (!studentLogFlag) return "redirect:/error";

        if (sas.appointmentExists(currentUserId)) {
            redirectAttributes.addFlashAttribute("successMessage", "Appointment already exists ❗");
            return "redirect:/student";
        }

        MappingDetails dm = mds.getById(currentUserId);
        if (dm == null) {
            redirectAttributes.addFlashAttribute("successMessage", "No counselor data found for your account.");
            return "redirect:/student";
        }

        sa.setCounselorId((int) dm.getCounselorId()); // counselorId as int
        sa.setStudentId(currentUserId);
        sas.storeAppointments(sa);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment Scheduled ✔");
        return "redirect:/student";
    }

    @GetMapping("/MeetingLink")
    public ModelAndView getMeetingLink() {
        if (!studentLogFlag) return new ModelAndView("Error");

        List<MeetingLink> myLinks = mls.getAllMeetings().stream()
                .filter(link -> link.getStudentId() == currentUserId)
                .toList();
        return new ModelAndView("viewmeetinglink", "ma", myLinks);
    }

    // ---------------- COUNSELOR PANEL ---------------- //

    @GetMapping("/counselor")
    public String counselorDashboard() {
        return counselorLogFlag ? "Counselor" : "redirect:/error";
    }

    @GetMapping("/myStudents")
    public ModelAndView getYourStudents() {
        if (!counselorLogFlag) return new ModelAndView("Error");

        List<MappingDetails> myStudents = mds.getAllRecords().stream()
                .filter(m -> m.getCounselorId() == (int) currentUserId)
                .toList();
        return new ModelAndView("myGroupStudents", "my", myStudents);
    }

    @GetMapping("/Meeting")
    public String meetingForm() {
        return counselorLogFlag ? "meetingLink" : "redirect:/error";
    }

    @PostMapping("/addMeeting")
    public String addNewMeetingLink(@ModelAttribute MeetingLink ml, RedirectAttributes redirectAttributes) {
        if (!counselorLogFlag) return "redirect:/error";

        if (mls.meetingForStudentExists(ml.getStudentId())) {
            redirectAttributes.addFlashAttribute("successMessage", "Meeting already exists ❗");
            return "redirect:/counselor";
        }

        MappingDetails dm = mds.getById(ml.getStudentId());
        if (dm == null) {
            redirectAttributes.addFlashAttribute("successMessage", "Student data not found.");
            return "redirect:/counselor";
        }

        ml.setCounselorId((int) currentUserId); // counselorId as int
        mls.storeMeetings(ml);
        redirectAttributes.addFlashAttribute("successMessage", "Meeting scheduled ✔");
        return "redirect:/counselor";
    }

    @GetMapping("/myAppointments")
    public ModelAndView getMyAppointments() {
        if (!counselorLogFlag) return new ModelAndView("Error");

        List<ScheduleAppointment> myApps = sas.getAllAppointments().stream()
                .filter(app -> app.getCounselorId() == (int) currentUserId)
                .toList();
        return new ModelAndView("myAppointments", "ma", myApps);
    }

    @GetMapping("/myChannel")
    public ModelAndView getMyChannel() {
        if (!counselorLogFlag) return new ModelAndView("Error");

        MappingDetails matchObj = mds.getAllRecords().stream()
                .filter(m -> m.getCounselorId() == (int) currentUserId)
                .findFirst().orElse(null);
        return new ModelAndView("telegramChannel", "tc", matchObj);
    }

    @RequestMapping("/deleteAppointment/{studentId}")
    public String deleteAppointment(@PathVariable long studentId, RedirectAttributes redirectAttributes) {
        if (!counselorLogFlag) return "redirect:/error";

        sas.deleteAppointment(studentId);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment Deleted ✔");
        return "redirect:/counselor";
    }

    // ---------------- ADMIN PANEL ---------------- //

    @GetMapping("/admin")
    public String adminPanel() {
        return adminLogFlag ? "Admin" : "redirect:/error";
    }

    @GetMapping("/addNewMapping")
    public String addMappingForm() {
        return adminLogFlag ? "AddMapping" : "redirect:/error";
    }

    @GetMapping("/editMappings")
    public ModelAndView editMappings() {
        return adminLogFlag
                ? new ModelAndView("ModifyMappings", "map1", mds.getAllRecords())
                : new ModelAndView("Error");
    }

    @GetMapping("/allMappings")
    public ModelAndView getAllMappings() {
        return adminLogFlag
                ? new ModelAndView("mappinglist", "map", mds.getAllRecords())
                : new ModelAndView("Error");
    }

    @RequestMapping("/deleteMapping/{studentId}")
    public String deleteMapping(@PathVariable long studentId) {
        if (!adminLogFlag) return "redirect:/error";
        mds.deleteMapping(studentId);
        return "redirect:/editMappings";
    }

    @RequestMapping("/editMapping/{studentId}")
    public String editMapping(@PathVariable long studentId, Model model) {
        if (!adminLogFlag) return "redirect:/error";
        model.addAttribute("map", mds.getById(studentId));
        return "editMapping";
    }

    @PostMapping("/addMapping")
    public String addNewMapping(@ModelAttribute MappingDetails md, RedirectAttributes redirectAttributes) {
        if (!adminLogFlag) return "redirect:/error";
        mds.storeToDB(md);
        redirectAttributes.addFlashAttribute("successMessage", "Mapping Updated ✔");
        return "redirect:/admin";
    }
}
