package com.github.laxika.magicalvibes.cards.e;

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

class EyeblightAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent creature -1/-1")
    void etbShrinksOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A 1/1 target dies to state-based actions")
    void oneOneTargetDies() {
        GrizzlyBears weakBear = new GrizzlyBears();
        weakBear.setPower(1);
        weakBear.setToughness(1);
        harness.addToBattlefield(player2, weakBear);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castAssassin(bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new EyeblightAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearsId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAssassin(UUID targetId) {
        harness.setHand(player1, List.of(new EyeblightAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
