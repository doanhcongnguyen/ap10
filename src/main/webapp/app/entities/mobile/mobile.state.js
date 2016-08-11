(function() {
    'use strict';

    angular
        .module('ap10App')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('mobile', {
            parent: 'entity',
            url: '/mobile',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ap10App.mobile.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mobile/mobiles.html',
                    controller: 'MobileController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('mobile');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('mobile-detail', {
            parent: 'entity',
            url: '/mobile/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'ap10App.mobile.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/mobile/mobile-detail.html',
                    controller: 'MobileDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('mobile');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Mobile', function($stateParams, Mobile) {
                    return Mobile.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'mobile',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('mobile-detail.edit', {
            parent: 'mobile-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mobile/mobile-dialog.html',
                    controller: 'MobileDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Mobile', function(Mobile) {
                            return Mobile.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('mobile.new', {
            parent: 'mobile',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mobile/mobile-dialog.html',
                    controller: 'MobileDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                model: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('mobile', null, { reload: true });
                }, function() {
                    $state.go('mobile');
                });
            }]
        })
        .state('mobile.edit', {
            parent: 'mobile',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mobile/mobile-dialog.html',
                    controller: 'MobileDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Mobile', function(Mobile) {
                            return Mobile.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mobile', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('mobile.delete', {
            parent: 'mobile',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/mobile/mobile-delete-dialog.html',
                    controller: 'MobileDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Mobile', function(Mobile) {
                            return Mobile.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('mobile', null, { reload: true });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
