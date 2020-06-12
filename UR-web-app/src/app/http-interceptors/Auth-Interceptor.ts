import {Injectable} from '@angular/core';
import {NgxSpinnerService} from 'ngx-spinner';
import {
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
} from '@angular/common/http';
import {finalize} from 'rxjs/operators';
import {User} from '../entity/User';
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private spinner: NgxSpinnerService) {}
 user:User | undefined ;
 getUserDetails():User {
    return JSON.parse(localStorage.getItem('currentUser')!);
 }
 intercept(req: HttpRequest<any>, next: HttpHandler) {
   this.spinner.show();
   this.user = this.getUserDetails();
   if (this.user != null ) {
     const headers = req.headers
        .set('Content-Type', 'application/json')
         .set('userId', this.user.id.toString())
         .set('authToken', this.user.authToken)
         .set('authUserId', this.user.urAdminAuthId);
     const authReq = req.clone({headers});
     return next.handle(authReq).pipe(
         finalize(() => {
           this.spinner.hide();
         }),
     );
   }
   return next.handle(req);
 }
}
