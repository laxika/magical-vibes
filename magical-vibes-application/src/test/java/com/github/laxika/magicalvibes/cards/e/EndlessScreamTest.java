package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CursedScroll;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndlessScream.class, MoggFanatic.class, CursedScroll.class})
class EndlessScreamTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 scream counters and gives the enchanted creature +3/+0")
    void entersWithXCountersAndBoosts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        harness.setHand(player1, List.of(new EndlessScream()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 3, bears.getId(), null);
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Endless Scream");
        assertThat(aura.getCounterCount(CounterType.SCREAM)).isEqualTo(3);
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X=0 gives no boost")
    void zeroCountersGivesNoBoost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        harness.setHand(player1, List.of(new EndlessScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Endless Scream");
        assertThat(aura.getCounterCount(CounterType.SCREAM)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base power when Endless Scream leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EndlessScream());
        aura.setCounterCount(CounterType.SCREAM, 4);
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Endless Scream can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new MoggFanatic());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EndlessScream());
        aura.setCounterCount(CounterType.SCREAM, 2);
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedScroll());

        harness.setHand(player1, List.of(new EndlessScream()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Endless Scream fizzles if the enchanted creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        harness.setHand(player1, List.of(new EndlessScream()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 2, bears.getId(), null);
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Endless Scream");
        harness.assertNotOnBattlefield(player1, "Endless Scream");
    }
}
