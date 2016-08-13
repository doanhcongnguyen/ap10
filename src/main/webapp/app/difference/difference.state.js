(function() {
    'use strict';

    angular
        .module('ap10App')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('difference', {
            parent: 'app',
            url: '/',
            data: {
                authorities: ['ROLE_ADMIN'],
                pageTitle: 'difference.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/difference/difference.html',
                    controller: 'DifferenceController',
                    controllerAs: 'vm'
                }
            },            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                }
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort)
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('user-management');
                    return $translate.refresh();
                }]

            }        })
        ;
    }
})();
