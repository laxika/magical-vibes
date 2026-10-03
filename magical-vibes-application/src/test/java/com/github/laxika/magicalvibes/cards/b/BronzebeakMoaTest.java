package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzebeakMoa.class, KraulWarrior.class})
class BronzebeakMoaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new BronzebeakMoa());

        castKraulWarrior(player1);
        harness.passBothPriorities(); // resolve creature spell (triggers the Moa)
        harness.passBothPriorities(); // resolve the boost triggered ability

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new BronzebeakMoa());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castKraulWarrior(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple creature entries")
    void boostStacksForMultipleCreatures() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new BronzebeakMoa());

        castKraulWarrior(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);

        castKraulWarrior(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(8);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new BronzebeakMoa());

        castKraulWarrior(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostItselfOnEntry() {
        harness.setHand(player1, List.of(new BronzebeakMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent moa = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, moa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, moa)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second Moa boosts only the Moa already on the battlefield")
    void secondMoaBoostsOnlyExistingMoa() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BronzebeakMoa());
        harness.setHand(player1, List.of(new BronzebeakMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entering)).isEqualTo(2);
    }

    private void castKraulWarrior(Player player) {
        harness.setHand(player, List.of(new KraulWarrior()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
