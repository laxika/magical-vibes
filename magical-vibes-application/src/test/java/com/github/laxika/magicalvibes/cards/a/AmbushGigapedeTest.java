package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmbushGigapede.class, GrizzlyBears.class, HillGiant.class})
class AmbushGigapedeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the targeted opponent creature -2/-2")
    void etbShrinksOpponentCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castGigapede(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A 1/1 target dies to state-based actions")
    void oneOneTargetDies() {
        GrizzlyBears weakBear = new GrizzlyBears();
        weakBear.setPower(1);
        weakBear.setToughness(1);
        harness.addToBattlefield(player2, weakBear);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castGigapede(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castGigapede(targetId);
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
        harness.setHand(player1, List.of(new AmbushGigapede()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGigapede(UUID targetId) {
        harness.setHand(player1, List.of(new AmbushGigapede()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
