package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LevitatingStatue.class, Shock.class, GrizzlyBears.class})
class LevitatingStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Levitating Statue")
    void noncreatureSpellPutsCounterOnStatue() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put a counter on Levitating Statue")
    void creatureSpellDoesNotPutCounterOnStatue() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Activating Levitating Statue makes it a 1/1 Construct artifact creature")
    void activationAnimatesStatue() {
        Permanent statue = addStatue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(statue.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, statue)).isTrue();
        assertThat(gqs.isArtifact(statue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(1);
        assertThat(statue.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    @Test
    @DisplayName("The counter trigger boosts the animated Statue and animation wears off at end of turn")
    void counterBoostsAnimatedStatueUntilEndOfTurn() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not add a counter")
    void opponentSpellDoesNotPutCounterOnStatue() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Casting another Statue triggers only the Statue already on the battlefield")
    void artifactSpellTriggersExistingStatueBeforeResolving() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.setHand(player1, List.of(new LevitatingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(statue);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent newStatue = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(newStatue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Repeated activation neither adds counters nor increases base power and toughness")
    void repeatedActivationDoesNotTriggerOrAccumulateStats() {
        Permanent statue = addStatue();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, statue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, statue)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Statue gets its own counter even while animated")
    void animatedStatuesEachReceiveTheirOwnCounter() {
        Permanent first = addStatue();
        Permanent second = addStatue();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, second)).isFalse();
    }

    private Permanent addStatue() {
        return harness.addToBattlefieldAndReturn(player1, new LevitatingStatue());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
