
/**
 * 由 Fantastic-admin 提供技术支持
 * Powered by Fantastic-admin
 * Gitee  https://gitee.com/hooray/fantastic-admin
 * Github https://github.com/hooray/fantastic-admin
 */
  
import{R as _,N as s,S as b,n as I,a as n,o as T,c as A,f as e,g as t,b as i,i as p,t as O,q as C,Q as S,_ as R,O as U}from"./index-04c83304.js";const y={data(){return{showEditDialog:!1,showCreateDialog:!1,dataSource:{},editDeptDetail:{},createDeptDetail:{},editFieldRules:{name:[{required:!0,message:"部门名称必填",trigger:"blur"}]}}},computed:{},mounted(){this.loadDepartmentTree()},methods:{loadDepartmentTree(){_.get("server/dept/getDeptTree").then(o=>{this.dataSource=o.data})},edit(o,a){this.editDeptDetail={deptId:o.key,parentId:o.data.parentId,name:o.label},this.showEditDialog=!0},cancelEditOperation(){this.showEditDialog=!1},saveEdit(){this.$refs.editDeptDetailForm.validate(o=>{o?_.post(`server/dept/modifyDept?deptId=${this.editDeptDetail.deptId}`,this.editDeptDetail).then(a=>{a.code===0?(s({type:"success",message:"保存成功"}),this.showEditDialog=!1,this.loadDepartmentTree()):s({type:"error",message:`编辑失败：${a.message}`})}):s({type:"error",message:"参数内容填写有误，请检查"})})},create(){this.createDeptDetail={},this.showCreateDialog=!0},cancelCreateOperation(){this.showCreateDialog=!1},saveCreateOperation(){this.$refs.createDeptDetailForm.validate(o=>{o?_.post("server/dept/createDept",this.createDeptDetail).then(a=>{a.code===0?(s({type:"success",message:"保存成功"}),this.showCreateDialog=!1,this.loadDepartmentTree()):s({type:"error",message:`编辑失败：${a.message}`})}):s({type:"error",message:"参数内容填写有误，请检查"})})},remove(o,a){if(!o.isLeaf){s({type:"error",message:"存在子节点，不允许删除"});return}b.confirm("确定要删除所选部门吗？此操作不可恢复！","Warning",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(()=>{_.get(`server/dept/deleteDept?deptId=${o.key}`).then(D=>{D.code===0?s({type:"success",message:"删除成功"}):s({type:"error",message:`删除失败：${D.message}`}),this.loadDepartmentTree()})}).catch(()=>{})}}};const M={class:"button-group"},N={class:"custom-tree-node"},q=["onClick"],z=["onClick"],L={class:"dialog-footer"},Q={class:"dialog-footer"};function W(o,a,D,j,l,r){const k=S,v=R,V=n("el-icon"),d=n("el-button"),w=n("el-tree"),B=U,f=n("el-tree-select"),c=n("el-form-item"),m=n("el-col"),E=n("el-input"),g=n("el-form"),h=n("el-dialog");return T(),A("div",null,[e(k,{title:"部门管理",content:"本页面提供对系统内所有的部门信息进行维护的能力，部门在系统内是一个重要基础数据，人员、项目都需要隶属于某个部门下。"}),e(B,null,{default:t(()=>[i("div",M,[e(d,{type:"primary",onClick:r.create},{icon:t(()=>[e(V,null,{default:t(()=>[e(v,{name:"ep:circle-plus"})]),_:1})]),default:t(()=>[p(" 新建 ")]),_:1},8,["onClick"])]),e(w,{data:l.dataSource,"node-key":"id","default-expand-all":"","expand-on-click-node":!1},{default:t(({node:u,data:F})=>[i("span",N,[i("span",null,O(u.label),1),i("span",null,[i("a",{onClick:x=>r.edit(u,F)}," 编辑 ",8,q),i("a",{style:{"margin-left":"8px"},onClick:x=>r.remove(u,F)}," 删除 ",8,z)])])]),_:1},8,["data"])]),_:1}),e(h,{modelValue:l.showCreateDialog,"onUpdate:modelValue":a[2]||(a[2]=u=>l.showCreateDialog=u),title:"新建部门"},{footer:t(()=>[i("span",L,[e(d,{onClick:r.cancelCreateOperation},{default:t(()=>[p(" 取消 ")]),_:1},8,["onClick"]),e(d,{type:"primary",onClick:r.saveCreateOperation},{default:t(()=>[p(" 保存 ")]),_:1},8,["onClick"])])]),default:t(()=>[e(g,{ref:"createDeptDetailForm",model:l.createDeptDetail,size:"default","label-width":"120px",rules:l.editFieldRules},{default:t(()=>[e(m,{span:24},{default:t(()=>[e(c,{label:"父部门"},{default:t(()=>[e(f,{modelValue:l.createDeptDetail.parentId,"onUpdate:modelValue":a[0]||(a[0]=u=>l.createDeptDetail.parentId=u),data:l.dataSource,"render-after-expand":!1,"check-strictly":"true"},null,8,["modelValue","data"])]),_:1})]),_:1}),e(m,{span:18},{default:t(()=>[e(c,{label:"部门名称",prop:"name",rules:l.editFieldRules.name},{default:t(()=>[e(E,{modelValue:l.createDeptDetail.name,"onUpdate:modelValue":a[1]||(a[1]=u=>l.createDeptDetail.name=u),placeholder:"输入部门名称",maxlength:"64","show-word-limit":""},null,8,["modelValue"])]),_:1},8,["rules"])]),_:1})]),_:1},8,["model","rules"])]),_:1},8,["modelValue"]),e(h,{modelValue:l.showEditDialog,"onUpdate:modelValue":a[5]||(a[5]=u=>l.showEditDialog=u),title:"编辑部门"},{footer:t(()=>[i("span",Q,[e(d,{onClick:r.cancelEditOperation},{default:t(()=>[p(" 取消 ")]),_:1},8,["onClick"]),e(d,{type:"primary",onClick:r.saveEdit},{default:t(()=>[p(" 保存 ")]),_:1},8,["onClick"])])]),default:t(()=>[e(g,{ref:"editDeptDetailForm",model:l.editDeptDetail,size:"default","label-width":"120px",rules:l.editFieldRules},{default:t(()=>[e(m,{span:24},{default:t(()=>[e(c,{label:"父部门"},{default:t(()=>[e(f,{modelValue:l.editDeptDetail.parentId,"onUpdate:modelValue":a[3]||(a[3]=u=>l.editDeptDetail.parentId=u),data:l.dataSource,"render-after-expand":!1,"check-strictly":"true"},null,8,["modelValue","data"])]),_:1})]),_:1}),e(m,{span:18},{default:t(()=>[e(c,{label:"部门名称",prop:"name",rules:l.editFieldRules.name},{default:t(()=>[e(E,{modelValue:l.editDeptDetail.name,"onUpdate:modelValue":a[4]||(a[4]=u=>l.editDeptDetail.name=u),placeholder:"输入部门名称",maxlength:"64","show-word-limit":""},null,8,["modelValue"])]),_:1},8,["rules"])]),_:1})]),_:1},8,["model","rules"])]),_:1},8,["modelValue"])])}typeof C=="function"&&C(y);const H=I(y,[["render",W],["__scopeId","data-v-a054963a"]]);export{H as default};
//# sourceMappingURL=dept-mgt-a156d954.js.map