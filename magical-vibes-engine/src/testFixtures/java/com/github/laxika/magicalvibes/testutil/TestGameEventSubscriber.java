package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.service.event.GameEventSubscriber;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test-fixture bridge for attaching module-specific observers to the shared engine context.
 *
 * <p>The engine test fixtures cannot depend on the AI module, while AI integration/soak tests
 * need their real subscriber to observe the same canonical batches as production. Each harness
 * reset clears delegates so observers never leak between tests.
 */
public final class TestGameEventSubscriber implements GameEventSubscriber {

    private final CopyOnWriteArrayList<GameEventSubscriber> delegates =
            new CopyOnWriteArrayList<>();

    public AutoCloseable subscribe(GameEventSubscriber subscriber) {
        delegates.add(subscriber);
        return () -> delegates.remove(subscriber);
    }

    public void reset() {
        delegates.clear();
    }

    @Override
    public void onGameEvents(GameEventBatch batch) {
        for (GameEventSubscriber delegate : delegates) {
            delegate.onGameEvents(batch);
        }
    }
}
