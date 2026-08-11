package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class LashOfTheWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-4 until end of turn")
    void givesTargetCreatureMinusFourMinusFour() {
        harness.addToBattlefield(player2, new Colossapede());
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID colossapedeId = harness.getPermanentId(player2, "Colossapede");
        harness.castInstant(player1, 0, colossapedeId);
        harness.passBothPriorities();

        Permanent colossapede = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(colossapede.getPowerModifier()).isEqualTo(-4);
        assertThat(colossapede.getToughnessModifier()).isEqualTo(-4);
        assertThat(colossapede.getEffectivePower()).isEqualTo(1);
        assertThat(colossapede.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with toughness 4 or less dies to the debuff")
    void creatureWithLowToughnessDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        harness.addToBattlefield(player2, new Colossapede());
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID colossapedeId = harness.getPermanentId(player2, "Colossapede");
        harness.castInstant(player1, 0, colossapedeId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent colossapede = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(colossapede.getPowerModifier()).isEqualTo(0);
        assertThat(colossapede.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new LashOfTheWhip()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
