package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.SeparateIntoPilesEffect;

import java.util.List;
import java.util.UUID;

/**
 * Queue-only state of a resolving {@link SeparateIntoPilesEffect} between its prompts: the separation of one group
 * (a multi-permanent or multi-graveyard choice of Pile 1) and its pile choice (a yes / no question, Yes = Pile 1).
 * The step records its result once the last group's pile is chosen.
 *
 * @param effect       the resolving step
 * @param separatorId  who separates: the effect's controller
 * @param group        the objects being separated now and who chooses between their piles
 * @param laterGroups  the groups separated after this one
 * @param chosenBefore the piles chosen for the earlier groups
 * @param pile1Ids     Pile 1, once separated; empty before
 * @param pile2Ids     Pile 2, once separated; empty before
 */
public record PendingPileStep(SeparateIntoPilesEffect effect, UUID separatorId, Group group, List<Group> laterGroups,
                              List<UUID> chosenBefore, List<UUID> pile1Ids, List<UUID> pile2Ids)
        implements PendingInteraction {

    /**
     * The objects of one separation and the player who chooses one of their piles.
     *
     * @param chooserId who chooses a pile
     * @param objectIds the permanents or cards separated, in the order they are offered
     */
    public record Group(UUID chooserId, List<UUID> objectIds) {

        public Group {
            objectIds = List.copyOf(objectIds);
        }
    }

    public PendingPileStep {
        laterGroups = List.copyOf(laterGroups);
        chosenBefore = List.copyOf(chosenBefore);
        pile1Ids = List.copyOf(pile1Ids);
        pile2Ids = List.copyOf(pile2Ids);
    }

    /** This state with the group separated into {@code pile1} and {@code pile2}. */
    public PendingPileStep separated(List<UUID> pile1, List<UUID> pile2) {
        return new PendingPileStep(effect, separatorId, group, laterGroups, chosenBefore, pile1, pile2);
    }
}
