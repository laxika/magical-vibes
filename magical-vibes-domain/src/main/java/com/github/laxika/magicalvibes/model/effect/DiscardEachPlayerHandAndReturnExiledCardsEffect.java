package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;

import java.util.List;

/**
 * Delayed end-step effect that makes each player discard their hand, then returns that player's
 * cards remembered by {@link ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect}.
 */
public record DiscardEachPlayerHandAndReturnExiledCardsEffect(
        List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> players) implements CardEffect {

    public DiscardEachPlayerHandAndReturnExiledCardsEffect {
        players = players == null ? List.of() : List.copyOf(players);
    }
}
