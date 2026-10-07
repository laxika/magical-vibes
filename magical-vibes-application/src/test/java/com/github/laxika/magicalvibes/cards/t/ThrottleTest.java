package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ElderscaleWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Throttle.class, AirElemental.class, ElderscaleWurm.class, FountainOfYouth.class})
class ThrottleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-4 until end of turn")
    void givesTargetCreatureMinusFourMinusFour() {
        Permanent target = addCreature(player2, new ElderscaleWurm());
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("Kills a creature whose toughness is reduced to zero")
    void killsCreatureWithZeroToughness() {
        Permanent target = addCreature(player2, new AirElemental());
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2, new ElderscaleWurm());
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreature(player1, new ElderscaleWurm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ElderscaleWurm());
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Elderscale Wurm");
        harness.assertInGraveyard(player1, "Throttle");
    }

    @Test
    @DisplayName("Two reductions accumulate and kill a larger creature")
    void reductionsAccumulate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElderscaleWurm());
        harness.setHand(player1, List.of(new Throttle(), new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Elderscale Wurm");

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elderscale Wurm");
        harness.assertInGraveyard(player2, "Elderscale Wurm");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Throttle).hasSize(2);
    }

    @Test
    @DisplayName("Does not affect another creature when its target dies in response")
    void targetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ElderscaleWurm());
        harness.setHand(player1, List.of(new Throttle(), new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Elderscale Wurm");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Throttle).hasSize(2);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
