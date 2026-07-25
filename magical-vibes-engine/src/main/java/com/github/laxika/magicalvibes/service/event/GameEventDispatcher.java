package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Failure-isolated fan-out for completed event batches.
 */
@Slf4j
@Component
public class GameEventDispatcher {

    private final List<GameEventSubscriber> subscribers;

    public GameEventDispatcher(List<GameEventSubscriber> subscribers) {
        this.subscribers = List.copyOf(subscribers);
    }

    public void dispatch(GameEventBatch batch) {
        if (batch.dispatchMode() == GameEventBatch.DispatchMode.SUPPRESSED_SIMULATION
                || batch.events().isEmpty()) {
            return;
        }

        for (GameEventSubscriber subscriber : subscribers) {
            try {
                subscriber.onGameEvents(batch);
            } catch (VirtualMachineError fatal) {
                throw fatal;
            } catch (Throwable failure) {
                // The mutation and sequence allocation are already committed. A broken observer
                // must neither rewind them nor prevent independent observers from running.
                log.error("Game event subscriber {} failed for game {} action {}",
                        subscriber.getClass().getName(), batch.gameId(), batch.causalActionId(), failure);
            }
        }
    }
}
