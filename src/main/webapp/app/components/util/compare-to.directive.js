(function() {
    'use strict';

    angular
        .module('ap10App')
        .directive('compareTo', compareTo);

    function compareTo() {

        var directive = {
            require: "ngModel",
            scope: {
                otherModelValue: "=compareTo"
            },
            link: function(scope, element, attributes, ngModel) {

                ngModel.$validators.compareTo = function(modelValue) {
                    if (modelValue instanceof Object) {
                        return angular.toJson(modelValue) == angular.toJson(scope.otherModelValue);
                    } else {
                        return modelValue == scope.otherModelValue;
                    }
                };

                scope.$watch("otherModelValue", function() {
                    ngModel.$validate();
                });
            }
        };

        return directive;
    }

})();
