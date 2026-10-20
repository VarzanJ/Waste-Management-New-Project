"use strict";

/* Shared Dark / Light Theme across all pages */
(function () {
    const themeToggle = document.getElementById("themeToggle");
    const storageKey = "ecosmart-theme";

    function getSavedTheme() {
        try {
            return localStorage.getItem(storageKey) || "light";
        } catch (error) {
            return "light";
        }
    }

    function saveTheme(theme) {
        try {
            localStorage.setItem(storageKey, theme);
        } catch (error) {
            // Theme still works for the current page if storage is unavailable.
        }
    }

    function updateThemeButton(theme) {
        if (!themeToggle) return;

        const darkMode = theme === "dark";

        themeToggle.textContent = darkMode
            ? "☀️ Light Mode"
            : "🌙 Dark Mode";

        themeToggle.setAttribute(
            "aria-label",
            darkMode ? "Switch to light theme" : "Switch to dark theme"
        );
    }

    const initialTheme = getSavedTheme() === "dark" ? "dark" : "light";

    document.documentElement.setAttribute("data-theme", initialTheme);
    updateThemeButton(initialTheme);

    if (themeToggle) {
        themeToggle.addEventListener("click", function () {
            const currentTheme =
                document.documentElement.getAttribute("data-theme") || "light";

            const nextTheme =
                currentTheme === "dark" ? "light" : "dark";

            document.documentElement.setAttribute("data-theme", nextTheme);
            saveTheme(nextTheme);
            updateThemeButton(nextTheme);
        });
    }
})();


/* Search table rows on pages that have the search input */
(function () {
    const searchInput = document.getElementById("searchInput");
    const reportsTable = document.getElementById("reportsTable");

    if (!searchInput || !reportsTable) return;

    searchInput.addEventListener("input", function () {
        const searchText = this.value.trim().toLowerCase();

        reportsTable.querySelectorAll("tbody tr").forEach(function (row) {
            if (row.querySelector(".empty")) return;

            row.hidden = !row.innerText.toLowerCase().includes(searchText);
        });
    });
})();


/* Prevent accidental repeated public report submissions */
(function () {
    const reportForm = document.getElementById("reportForm");

    if (!reportForm) return;

    let submitted = false;

    reportForm.addEventListener("submit", function (event) {
        if (submitted) {
            event.preventDefault();
            return;
        }

        if (!reportForm.checkValidity()) {
            return;
        }

        submitted = true;

        const submitButton = reportForm.querySelector(
            'button[type="submit"]'
        );

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Submitting...";
        }
    });
})();


/* Confirm destructive actions and prevent repeated clicks */
(function () {
    document.querySelectorAll(".delete-form").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            const confirmed = window.confirm(
                "Are you sure you want to delete this report? This cannot be undone."
            );

            if (!confirmed) {
                event.preventDefault();
                return;
            }

            const button = form.querySelector('button[type="submit"]');

            if (button) {
                button.disabled = true;
                button.textContent = "Deleting...";
            }
        });
    });

    document.querySelectorAll(
        'form[action*="/status"]'
    ).forEach(function (form) {
        form.addEventListener("submit", function () {
            const button = form.querySelector('button[type="submit"]');

            if (button) {
                button.disabled = true;
                button.textContent = "Updating...";
            }
        });
    });
})();