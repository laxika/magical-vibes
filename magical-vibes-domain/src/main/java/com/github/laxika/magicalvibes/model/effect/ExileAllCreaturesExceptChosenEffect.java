package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Exiles every creature on the battlefield except the chosen creatures. */
public record ExileAllCreaturesExceptChosenEffect(List<UUID> chosenCreatureIds)
        implements BoardWipeEffect {

    public ExileAllCreaturesExceptChosenEffect {
        chosenCreatureIds = List.copyOf(chosenCreatureIds);
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
