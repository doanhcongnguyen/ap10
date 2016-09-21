(function() {
    'use strict';

    angular
        .module('ap10App')
        .controller('NavbarController', NavbarController);

    NavbarController.$inject = ['$state', 'Auth', 'Principal', 'ProfileService'];

    function NavbarController ($state, Auth, Principal, ProfileService) {
        var vm = this;

        vm.isNavbarCollapsed = true;
        vm.isAuthenticated = Principal.isAuthenticated;
        vm.currentIdentity = Principal.currentIdentity;

        ProfileService.getProfileInfo().then(function(response) {
            vm.inProduction = response.inProduction;
        });

        vm.logout = logout;
        vm.toggleNavbar = toggleNavbar;
        vm.collapseNavbar = collapseNavbar;
        vm.$state = $state;

        function logout() {
            collapseNavbar();
            Auth.logout();
            $state.go('login');
        }

        function toggleNavbar() {
            vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
        }

        function collapseNavbar() {
            vm.isNavbarCollapsed = true;
        }
    }
})();
