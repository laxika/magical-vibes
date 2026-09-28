package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Progress state for Expert-Level Safe's two-player hidden-number choice. */
public class ExpertLevelSafeState {

    public boolean active;
    public UUID sourcePermanentId;
    public UUID controllerId;
    public UUID opponentId;
    public UUID currentPlayerId;
    public Integer controllerChoice;
    public Integer opponentChoice;

    public void reset() {
        active = false;
        sourcePermanentId = null;
        controllerId = null;
        opponentId = null;
        currentPlayerId = null;
        controllerChoice = null;
        opponentChoice = null;
    }
}
