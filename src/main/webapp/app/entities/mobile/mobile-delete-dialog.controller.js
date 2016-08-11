(function() {
    'use strict';

    angular
        .module('ap10App')
        .controller('MobileDeleteController',MobileDeleteController);

    MobileDeleteController.$inject = ['$uibModalInstance', 'entity', 'Mobile'];

    function MobileDeleteController($uibModalInstance, entity, Mobile) {
        var vm = this;

        vm.mobile = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        
        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Mobile.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
