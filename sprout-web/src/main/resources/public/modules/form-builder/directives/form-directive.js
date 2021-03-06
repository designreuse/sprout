'use strict';

angular.module('form-builder').directive('formDirective', [function () {
    return {
        controller: function($scope){
            $scope.submitHandler = function(){
                $scope.form.submitted = true;
                if($scope.onSubmit){
                    $scope.onSubmit();
                }
            }

            $scope.cancelHandler = function(){
                if($scope.onCancel){
                	$scope.onCancel();
                }
            }
        },
        templateUrl: 'modules/form-builder/views/directive-templates/form/form.html',
        restrict: 'E',
        scope: {
            form:'=form',
            onSubmit: '&onSubmit',
            onCancel: '&onCancel',
            showCancel: '=showCancel'
        }
    };
  }]);
