package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodForTheBloodGod.class, GrizzlyBears.class, Shock.class})
class BloodForTheBloodGodTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each creature that died this turn and resolves all effects")
    void costsLessForCreatureDeathAndResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new BloodForTheBloodGod(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Blood for the Blood God!"));
        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Blood for the Blood God!");
    }

    @Test
    @DisplayName("Cannot use the reduced cost when no creature died this turn")
    void requiresFullCostWithoutCreatureDeath() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new BloodForTheBloodGod()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty hand still draws eight cards, damages only the opponent, and exiles the spell")
    void resolvesWithEmptyHandAtFullCost() {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new BloodForTheBloodGod()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof BloodForTheBloodGod);
    }

    @Test
    @DisplayName("Deaths of creatures controlled by either player each reduce the cost")
    void countsDeathsOnBothSides() {
        prepareMainPhase(player1);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new BloodForTheBloodGod()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, ownBears.getId());
        harness.castAndResolveInstant(player1, 0, opposingBears.getId());
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof BloodForTheBloodGod);
    }

    @Test
    @DisplayName("More than eight deaths remove all generic mana but do not reduce colored requirements")
    void excessDeathsDoNotReduceColoredMana() {
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        for (int i = 0; i < 9; i++) {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Shock()));
            harness.castAndResolveInstant(player1, 0, bears.getId());
        }
        harness.setHand(player1, List.of(new BloodForTheBloodGod()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof BloodForTheBloodGod);
    }

    @Test
    @DisplayName("Discards every remaining card before drawing, without discarding the opponent's hand")
    void discardsEntireOwnHandBeforeDrawing() {
        prepareMainPhase(player1);
        Shock discardedShock = new Shock();
        GrizzlyBears discardedBears = new GrizzlyBears();
        Shock opposingCard = new Shock();
        harness.setHand(player1, List.of(new BloodForTheBloodGod(), discardedShock, discardedBears));
        harness.setHand(player2, List.of(opposingCard));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedShock, discardedBears);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8)
                .doesNotContain(discardedShock, discardedBears);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card instanceof BloodForTheBloodGod);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
