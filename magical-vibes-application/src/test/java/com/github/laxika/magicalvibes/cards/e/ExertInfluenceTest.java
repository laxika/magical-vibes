package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExertInfluence.class, Forest.class, CoralhelmGuide.class, MistIntruder.class})
class ExertInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is within the one-color Converge value")
    void gainsControlWithOneColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistIntruder());

        castWithMana(ManaColor.BLUE, 5, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Gains control of a creature whose power equals the two-color Converge value")
    void gainsControlWithTwoColors() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Leaves a creature with greater power under its controller's control")
    void doesNotGainControlWhenPowerExceedsConvergeValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        castWithMana(ManaColor.BLUE, 5, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Colorless mana does not increase Converge")
    void colorlessManaDoesNotCountAsAColor() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Five colors spent can steal a creature with five power")
    void gainsControlWithFiveColors() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        for (ManaColor color : ManaColor.COLORS) {
            harness.addMana(player1, color, 1);
        }

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Checks increased power at resolution rather than casting")
    void increasedPowerBeforeResolutionPreventsControlChange() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistIntruder());
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("A creature above the threshold can be targeted and stolen if its power decreases")
    void decreasedPowerBeforeResolutionAllowsControlChange() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistIntruder());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Control persists through later power increases and the end of the turn")
    void controlDoesNotExpireOrRecheckPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MistIntruder());
        castWithMana(ManaColor.BLUE, 5, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private void castWithMana(ManaColor color, int amount, java.util.UUID targetId) {
        harness.setHand(player1, java.util.List.of(new ExertInfluence()));
        harness.addMana(player1, color, amount);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
