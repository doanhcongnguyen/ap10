(function() {
    'use strict';
    angular
        .module('ap10App')
        .factory('Mobile', Mobile);

    Mobile.$inject = ['$resource'];

    function Mobile ($resource) {
        var resourceUrl =  'api/mobiles/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
