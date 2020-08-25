package org.chaos;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Try monad
 */
abstract public class JavaTry<V> {

    private JavaTry(){}

    abstract public boolean isSuccess();
    abstract public boolean isFailure();
    abstract public void throwException();

    public <U> JavaTry<U> map(Function<V,U> mapper) {
        Objects.requireNonNull(mapper);
        if (isFailure())
            return failure(failureException());
        else {
            V v  = successValue();
            try{
                return JavaTry.success(Objects.requireNonNull(mapper.apply(v)));
            }catch(Throwable e){
                return JavaTry.failure(e);
            }
        }
    }

    public JavaTry<V> filter(Function<V,Boolean> predicate) {
        Objects.requireNonNull(predicate);
        if (isFailure()) {
            return failure(failureException());
        } else {
            V v = successValue();
            try{
                return predicate.apply(v) ? JavaTry.success(v) : JavaTry.lift(null);
            }catch(Throwable e){
                return JavaTry.failure(e);
            }
        }
    }

    public <U> JavaTry<U> flatMap(Function<V, JavaTry<U>> mapper){
        Objects.requireNonNull(mapper);
        if (isFailure())
            return failure(failureException());
        else {
            V v  = successValue();
            try{
                return Objects.requireNonNull(mapper.apply(v));
            }catch(Throwable e){
                return JavaTry.failure(e);
            }
        }
    }

    public JavaTry<V> ifPresent(Consumer<V> c) {
        if (isSuccess()) {
            c.accept(successValue());
        }
        return this;
    }

    public void ifPresentOrThrow(Consumer<V> c) {
        if (isSuccess()) {
            c.accept(successValue());
        } else {
            throwException();
        }
    }

    public JavaTry<V> ifThrowable(Consumer<Throwable> c){
        if(isFailure()){
            c.accept(failureException());
        }
        return this;
    }

    public V getOrThrow(){
        if (isSuccess()) {
            return successValue();
        } else {
            throw failureException();
        }
    }

    public Optional<V> optional(){
        if (isSuccess()) {
            return Optional.of(successValue());
        } else {
            return Optional.empty();
        }
    }

    public static <V> JavaTry<V> failure(String message) {
        return new Failure<>(message);
    }

    public static <V> JavaTry<V> failure(String message, Throwable e) {
        return new Failure<>(message, e);
    }

    public static <V> JavaTry<V> failure(Throwable e) {
        return new Failure<>(e);
    }

    public static <V> JavaTry<V> lift(V value) {
        if(value==null)
            return failure(new NullPointerException("value must not be empty!"));
        return success(value);
    }

    public static <V> JavaTry<V> success(V value) {
        return new Success<>(value);
    }

    public static <V> JavaTry<V> supplier(Supplier<V> supplier){
        Objects.requireNonNull(supplier);
        try{
            return JavaTry.success(supplier.get());
        }catch(Throwable t){
            return JavaTry.failure(t);
        }
    }

    public static <V extends Collection<?>> JavaTry<V> collMustHaveSomeOne(V coll){
        if(coll==null || coll.size()==0)
            return JavaTry.failure("size of collection must be > 0");
        return JavaTry.success(coll);
    }

    public static <V> JavaTry<V> test(V value, Predicate<V> tester){
        if(tester.test(value))
            return success(value);
        else
            return failure("no passed the test");
    }

    public static <V> JavaTry<V> test(V value, Predicate<V> tester, String message){
        if(tester.test(value))
            return success(value);
        else
            return failure(message);
    }

    public static <V,U> JavaTry<U> tried(V v, Function<V,U> mapper){
        Objects.requireNonNull(mapper);
        try{
            return Objects.requireNonNull(JavaTry.success(mapper.apply(v)));
        }catch(Throwable e){
            return JavaTry.failure(e);
        }
    }

    public V successValue(){
        return ((Success<V>)this).value;
    }

    public RuntimeException failureException(){
        return ((Failure<V>)this).exception;
    }

    private static class Success<V> extends JavaTry<V> {

        private V value;

        public Success(V v){
            super();
            this.value = v;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public void throwException() {

        }
    }

    private static class Failure<V> extends JavaTry<V> {

        private RuntimeException exception;

        public Failure(String message) {
            super();
            this.exception = new IllegalStateException(message);
        }
        public Failure(String message, Throwable e) {
            super();
            this.exception = new IllegalStateException(message, e);
        }
        public Failure(Throwable e) {
            super();
            this.exception = new IllegalStateException(e);
        }


        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public void throwException() {
            throw this.exception;
        }
    }

}
