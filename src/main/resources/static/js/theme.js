(function () {
    var storageKey = 'theme';
    var theme = 'dark';
    try {
        theme = localStorage.getItem(storageKey) || 'dark';
    } catch (e) {}
    document.documentElement.setAttribute('data-theme', theme);

    document.addEventListener('DOMContentLoaded', function () {

        // Dark / Light button
        document.querySelectorAll('.theme-toggle').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var current = document.documentElement.getAttribute('data-theme');
                var next = current === 'light' ? 'dark' : 'light';
                document.documentElement.setAttribute('data-theme', next);
                try {
                    localStorage.setItem(storageKey, next);
                } catch (e) {}
            });
        });

        // Double click thadukkum: submit pannina button disable aagum
        document.querySelectorAll('form[method="post"]').forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (event.defaultPrevented) return;
                var button = form.querySelector('button[type="submit"]');
                if (button) {
                    setTimeout(function () { button.disabled = true; }, 0);
                }
            });
        });
    });

    // Back button azhuththina, disable aana buttons thirumba work aagum
    window.addEventListener('pageshow', function () {
        document.querySelectorAll('form[method="post"] button[type="submit"]').forEach(function (b) {
            b.disabled = false;
        });
    });
})();