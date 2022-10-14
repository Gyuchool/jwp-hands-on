package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

public class TransactionHandler implements InvocationHandler {


    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final var declaredMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Transactional.class)) {
            invokeWithTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private void invokeWithTransaction(Method method, Object[] args) {
        final var status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        try {
            method.invoke(target, args);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
