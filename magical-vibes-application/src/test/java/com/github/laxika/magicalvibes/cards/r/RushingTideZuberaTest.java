package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChoiceOfDamnations;
import com.github.laxika.magicalvibes.cards.g.GhostLitRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RushingTideZubera.class, GhostLitRaider.class, ChoiceOfDamnations.class})
class RushingTideZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it dies after being dealt four damage this turn")
    void drawsThreeCardsAfterFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new RushingTideZubera());
        harness.setLibrary(player1, List.of(new RushingTideZubera(), new RushingTideZubera(), new RushingTideZubera()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new GhostLitRaider()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player2, 0, zubera.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Rushing-Tide Zubera");
    }

    @Test
    @DisplayName("Does not draw when it dies after being dealt less than four damage this turn")
    void doesNotDrawAfterLessThanFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player2, new RushingTideZubera());
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        addCreatureReady(player1, new GhostLitRaider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, zubera.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ChoiceOfDamnations()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);
        harness.handleMayAbilityChosen(player1, false);
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMultiplePermanentsChosen(player2, List.of(zubera.getId()));
        }

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        harness.assertInGraveyard(player2, "Rushing-Tide Zubera");
    }

    @Test
    @DisplayName("Counts damage dealt in multiple packets during the same turn")
    void countsDamageAcrossMultiplePacketsThisTurn() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new RushingTideZubera());
        harness.setLibrary(player1, List.of(new RushingTideZubera(), new RushingTideZubera(), new RushingTideZubera()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        addCreatureReady(player2, new GhostLitRaider());
        addCreatureReady(player2, new GhostLitRaider());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, zubera.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 1, null, zubera.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Rushing-Tide Zubera");
    }
}
