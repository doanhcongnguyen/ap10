(function() {
    'use strict';

    angular
        .module('ap10App')
        .controller('MobileDetailController', MobileDetailController);

    MobileDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Mobile'];

    function MobileDetailController($scope, $rootScope, $stateParams, previousState, entity, Mobile) {
        var vm = this;

        vm.mobile = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ap10App:mobileUpdate', function(event, result) {
            vm.mobile = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
