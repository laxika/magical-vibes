package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitanicGrowth.class, RuneclawBear.class, Manalith.class, Unsummon.class})
class TitanicGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Titanic Growth gives +4/+4 to target creature")
    void resolvesAndBoostsTarget() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost from Titanic Growth wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Titanic Growth")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Manalith");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Titanic Growth can boost an opponent's creature without boosting other creatures")
    void boostsOnlyOpponentsTarget() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Titanic Growth");
    }

    @Test
    @DisplayName("Multiple Titanic Growth spells add their boosts together")
    void boostsAreCumulative() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth(), new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(10);
        assertThat(target.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("Titanic Growth does not resolve when its target is returned to hand in response")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Runeclaw Bear");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Titanic Growth");
        harness.assertInGraveyard(player2, "Unsummon");
        assertThat(otherBear.getEffectivePower()).isEqualTo(2);
        assertThat(otherBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
