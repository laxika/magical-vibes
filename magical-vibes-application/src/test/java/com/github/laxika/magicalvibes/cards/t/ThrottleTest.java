package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.ElderscaleWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
