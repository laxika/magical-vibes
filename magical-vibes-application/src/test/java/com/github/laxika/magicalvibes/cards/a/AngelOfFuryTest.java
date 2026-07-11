package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class AngelOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("When Angel of Fury dies and the may ability is accepted, it is shuffled into its owner's library")
    void diesAndAcceptShufflesIntoLibrary() {
        harness.setLibrary(player2, new ArrayList<>());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfFury());
        // Mark lethal damage (3/5) and let state-based actions destroy it.
        angel.setMarkedDamage(5);

        harness.runStateBasedActions();

        // It actually enters the graveyard; the death trigger waits on the stack.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel of Fury"));
        assertThat(gd.stack).isNotEmpty();

        // Resolve the MayEffect from the stack → may prompt for the owner.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        // Accept — the source is shuffled from the graveyard into its owner's library.
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotInGraveyard(player2, "Angel of Fury");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel of Fury"));
    }

    @Test
    @DisplayName("When Angel of Fury dies and the may ability is declined, it stays in its owner's graveyard")
    void diesAndDeclineStaysInGraveyard() {
        harness.setLibrary(player2, new ArrayList<>());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfFury());
        angel.setMarkedDamage(5);

        harness.runStateBasedActions();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // Decline the may ability — the card remains in the graveyard.
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel of Fury"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Angel of Fury"));
    }
}
