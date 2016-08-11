(function() {
    'use strict';

    angular
        .module('ap10App')
        .controller('TagDetailController', TagDetailController);

    TagDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Tag', 'Entry'];

    function TagDetailController($scope, $rootScope, $stateParams, previousState, entity, Tag, Entry) {
        var vm = this;

        vm.tag = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ap10App:tagUpdate', function(event, result) {
            vm.tag = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
