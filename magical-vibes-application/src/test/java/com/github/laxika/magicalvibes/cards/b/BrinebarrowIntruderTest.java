package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class BrinebarrowIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent creature -2/-0")
    void etbWeakensOpponentCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castIntruder(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castIntruder(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BrinebarrowIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castIntruder(UUID targetId) {
        harness.setHand(player1, List.of(new BrinebarrowIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
