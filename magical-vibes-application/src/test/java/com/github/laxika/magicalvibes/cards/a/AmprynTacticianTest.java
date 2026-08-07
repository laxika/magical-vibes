package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmprynTacticianTest extends BaseCardTest {

    private void castTactician() {
        harness.setHand(player1, List.of(new AmprynTactician()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering boosts other creatures you control and itself")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castTactician();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);

        Permanent tactician = findPermanent(player1, "Ampryn Tactician");
        assertThat(tactician.getEffectivePower()).isEqualTo(4);
        assertThat(tactician.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTactician();

        assertThat(findPermanent(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castTactician();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Ampryn Tactician").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Ampryn Tactician").getEffectiveToughness()).isEqualTo(3);
    }
}
