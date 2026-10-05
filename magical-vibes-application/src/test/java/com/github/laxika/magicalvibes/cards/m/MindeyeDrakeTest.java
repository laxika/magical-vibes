package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindeyeDrake.class, DoomBlade.class})
class MindeyeDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("When Mindeye Drake dies, target player mills five cards")
    void deathMillsTargetPlayerFive() {
        harness.addToBattlefield(player2, new MindeyeDrake());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        UUID drakeId = harness.getPermanentId(player2, "Mindeye Drake");
        harness.castInstant(player1, 0, drakeId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    @DisplayName("Death trigger can target the Drake's own controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player2, new MindeyeDrake());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        UUID drakeId = harness.getPermanentId(player2, "Mindeye Drake");
        harness.castInstant(player1, 0, drakeId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 5);
    }

    @Test
    @DisplayName("Death trigger mills the top five cards into the chosen player's graveyard")
    void millsTopFiveCardsOnly() {
        List<MindeyeDrake> library = List.of(new MindeyeDrake(), new MindeyeDrake(),
                new MindeyeDrake(), new MindeyeDrake(), new MindeyeDrake(), new MindeyeDrake());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of());
        int otherLibrarySize = gd.playerDecks.get(player2.getId()).size();
        var drake = harness.addToBattlefieldAndReturn(player2, new MindeyeDrake());
        drake.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mindeye Drake");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(5));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 5));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(otherLibrarySize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Death trigger mills all remaining cards when fewer than five remain")
    void millsShortLibrary() {
        List<MindeyeDrake> library = List.of(new MindeyeDrake(), new MindeyeDrake());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of());
        var drake = harness.addToBattlefieldAndReturn(player2, new MindeyeDrake());
        drake.setMarkedDamage(5);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
