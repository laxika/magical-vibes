package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, statue)).isFalse();
        assertThat(statue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
