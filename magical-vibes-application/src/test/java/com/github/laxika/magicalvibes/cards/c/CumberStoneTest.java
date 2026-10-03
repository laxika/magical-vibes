package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CumberStone.class, CylianSunsinger.class})
class CumberStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures get -1/-0")
    void weakensOpponentCreatures() {
        harness.addToBattlefield(player1, new CumberStone());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect the controller's own creatures")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new CumberStone());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Cumber Stones give opponent creatures -2/-0")
    void twoCumberStonesStack() {
        harness.addToBattlefield(player1, new CumberStone());
        harness.addToBattlefield(player1, new CumberStone());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Penalty is removed when Cumber Stone leaves the battlefield")
    void penaltyRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new CumberStone());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Cumber Stone"));

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stacking penalties can reduce power below zero without reducing toughness")
    void powerCanBecomeNegative() {
        harness.addToBattlefield(player1, new CumberStone());
        harness.addToBattlefield(player1, new CumberStone());
        harness.addToBattlefield(player1, new CumberStone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each player's Cumber Stone weakens only the other player's creatures")
    void opposingStonesAffectOnlyTheirOpponents() {
        harness.addToBattlefield(player1, new CumberStone());
        harness.addToBattlefield(player2, new CumberStone());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CylianSunsinger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }
}
