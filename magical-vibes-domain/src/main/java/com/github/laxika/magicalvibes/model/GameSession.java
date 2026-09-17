package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/** Owns a stack of independently mutable games. Access is serialized by the session lock. */
public final class GameSession {
    public final ReentrantLock lock = new ReentrantLock();
    private final List<GameData> frames = new ArrayList<>();
    private long activationEpoch;
    public boolean transitioning;
    public boolean transitionFailed;

    public GameSession(GameData root) {
        frames.add(root);
    }

    public GameData root() { return frames.getFirst(); }
    public GameData active() { return frames.getLast(); }
    public List<GameData> frames() { return List.copyOf(frames); }
    public int depth() { return frames.size() - 1; }
    public boolean isRoot(GameData game) { return root() == game; }
    public GameContext context() { return new GameContext(root().id, active().id, activationEpoch); }

    public void push(GameData child) {
        child.session = this;
        frames.add(child);
        activationEpoch++;
    }

    public GameData pop() {
        if (frames.size() == 1) throw new IllegalStateException("Cannot pop the root game");
        GameData child = frames.removeLast();
        activationEpoch++;
        return child;
    }

    /** Copies every frame once, sharing only frozen cards and immutable values. */
    public GameData simulationCopy(UUID requestedGameId) {
        GameSession result = null;
        GameData requested = null;
        for (GameData frame : frames) {
            GameData copy = frame.simulationFrameCopy();
            if (result == null) result = copy.session;
            else result.push(copy);
            if (frame.id.equals(requestedGameId)) requested = copy;
        }
        result.activationEpoch = activationEpoch;
        // A rollout may still hold its starting child after that child has returned to a parent.
        return requested != null ? requested : result.active();
    }
}
