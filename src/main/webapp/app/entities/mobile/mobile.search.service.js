(function() {
    'use strict';

    angular
        .module('ap10App')
        .factory('MobileSearch', MobileSearch);

    MobileSearch.$inject = ['$resource'];

    function MobileSearch($resource) {
        var resourceUrl =  'api/_search/mobiles/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
