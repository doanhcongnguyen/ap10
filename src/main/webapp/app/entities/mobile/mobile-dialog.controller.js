(function() {
    'use strict';

    angular
        .module('ap10App')
        .controller('MobileDialogController', MobileDialogController);

    MobileDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Mobile'];

    function MobileDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Mobile) {
        var vm = this;

        vm.mobile = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.mobile.id !== null) {
                Mobile.update(vm.mobile, onSaveSuccess, onSaveError);
            } else {
                Mobile.save(vm.mobile, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ap10App:mobileUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
