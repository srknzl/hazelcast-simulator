package com.hazelcast.simulator.protocol.operation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

/**
 * Defines the operation type for a {@link com.hazelcast.simulator.protocol.core.SimulatorMessage}.
 */
public enum OperationType {

    INTEGRATION_TEST(IntegrationTestOperation.class, 0),

    CREATE_WORKER(CreateWorkerOperation.class, 1),
    CREATE_TEST(CreateTestOperation.class, 2),

    LOG(LogOperation.class, 3),
    EXCEPTION(ExceptionOperation.class, 4),

    IS_PHASE_COMPLETED(IsPhaseCompletedOperation.class, 5),

    START_TEST_PHASE(StartTestPhaseOperation.class, 6),

    START_TEST(StartTestOperation.class, 7),

    STOP_TEST(StopTestOperation.class, 8);

    private final Class<? extends SimulatorOperation> classType;
    private final int classId;

    OperationType(Class<? extends SimulatorOperation> classType, int classId) {
        this.classType = classType;
        this.classId = classId;

        OperationTypeRegistry.register(this, classType, classId);
    }

    /**
     * Returns the {@link OperationType} of a {@link SimulatorOperation}.
     *
     * @param operation the {@link SimulatorOperation}
     * @return the {@link OperationType} of the {@link SimulatorOperation}
     */
    public static OperationType getOperationType(SimulatorOperation operation) {
        OperationType operationType = OperationTypeRegistry.OPERATION_TYPES.get(operation.getClass());
        if (operationType == null) {
            throw new IllegalArgumentException(format("Operation %s has not been registered!", operation.getClass().getName()));
        }
        return operationType;
    }

    /**
     * Returns the {@link OperationType} of a registered classId.
     *
     * @param classId the registered classId
     * @return the {@link OperationType}
     */
    public static OperationType fromInt(int classId) {
        OperationType operationType = OperationTypeRegistry.CLASS_IDS.get(classId);
        if (operationType == null) {
            throw new IllegalArgumentException(format("ClassId %d has not been registered!", classId));
        }
        return operationType;
    }

    /**
     * Returns the registered classId of a {@link OperationType}.
     *
     * @return the registered classId
     */
    public int toInt() {
        return classId;
    }

    /**
     * Returns the registered {@link Class} of the {@link OperationType} to deserialize a {@link SimulatorOperation}.
     *
     * @return the {@link Class} of the {@link OperationType}
     */
    public Class<? extends SimulatorOperation> getClassType() {
        return classType;
    }

    /**
     * Stores and validates the registered {@link OperationType} entries.
     *
     * This class prevents double registration of class types or classIds, which would produce late failures during runtime.
     */
    @SuppressWarnings("PMD.UnusedModifier")
    static class OperationTypeRegistry {

        private static final ConcurrentMap<Integer, OperationType> CLASS_IDS = new ConcurrentHashMap<Integer, OperationType>();

        private static final ConcurrentMap<Class<? extends SimulatorOperation>, OperationType> OPERATION_TYPES
                = new ConcurrentHashMap<Class<? extends SimulatorOperation>, OperationType>();

        static void register(OperationType operationType, Class<? extends SimulatorOperation> classType, int classId) {
            if (classId < 0) {
                throw new IllegalArgumentException("classId must be a positive number");
            }

            OperationType oldType = CLASS_IDS.putIfAbsent(classId, operationType);
            if (oldType != null) {
                throw new IllegalStateException(format("classId %d is already registered to %s", classId, oldType));
            }

            oldType = OPERATION_TYPES.putIfAbsent(classType, operationType);
            if (oldType != null) {
                throw new IllegalStateException(format("classType %s is already registered to %s", classType.getName(), oldType));
            }
        }
    }
}
