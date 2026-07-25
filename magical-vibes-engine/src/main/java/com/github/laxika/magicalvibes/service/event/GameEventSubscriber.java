package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.event.GameEventBatch;

/**
 * Post-mutation consumer of transport-independent game events.
 *
 * <p>Subscribers must enforce each envelope's audience when adapting facts to an external
 * transport. They receive no GameData reference; an adapter that needs current state resolves it
 * by game id after dispatch.
 */
@FunctionalInterface
public interface GameEventSubscriber {

    void onGameEvents(GameEventBatch batch);
}
