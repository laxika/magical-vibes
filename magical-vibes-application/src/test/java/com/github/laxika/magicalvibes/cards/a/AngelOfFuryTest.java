package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AngelOfFury.class)
class AngelOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("When Angel of Fury dies and the may ability is accepted, it is shuffled into its owner's library")
    void diesAndAcceptShufflesIntoLibrary() {
        harness.setLibrary(player2, new ArrayList<>());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfFury());
        var angelId = angel.getCard().getId();
        // Mark lethal damage (3/5) and let state-based actions destroy it.
        angel.setMarkedDamage(5);

        harness.runStateBasedActions();

        // It actually enters the graveyard; the death trigger waits on the stack.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(angelId));
        assertThat(gd.stack).isNotEmpty();

        // Resolve the MayEffect from the stack → may prompt for the owner.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        // Accept — the source is shuffled from the graveyard into its owner's library.
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(angelId));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(angelId));
    }

    @Test
    @DisplayName("When Angel of Fury dies and the may ability is declined, it stays in its owner's graveyard")
    void diesAndDeclineStaysInGraveyard() {
        harness.setLibrary(player2, new ArrayList<>());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfFury());
        var angelId = angel.getCard().getId();
        angel.setMarkedDamage(5);

        harness.runStateBasedActions();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        // Decline the may ability — the card remains in the graveyard.
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(angelId));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(angelId));
    }

    @Test
    @DisplayName("When a player controls an opponent-owned Angel of Fury, its accepted trigger uses the owner's library")
    void diesAndAcceptShufflesIntoOwnersLibrary() {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());

        AngelOfFury card = new AngelOfFury();
        card.setOwnerId(player1.getId());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, card);
        var angelId = card.getId();
        angel.setMarkedDamage(5);

        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(angelId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(angelId));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(angelId));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(angelId));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(angelId));
    }
}
