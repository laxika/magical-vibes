package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NebelgastIntruder.class, GrizzlyBears.class})
class NebelgastIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives up to one opposing creature -2/-0")
    void etbWeakensTargetedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castIntruder(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB target may be omitted")
    void etbTargetIsOptional() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new NebelgastIntruder(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castIntruder(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NebelgastIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Intruder can enter when the opponent controls no creatures")
    void entersWithoutAnyLegalTargets() {
        harness.castFromHand(player1, new NebelgastIntruder(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nebelgast Intruder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB still resolves after Intruder leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castIntruder(target.getId());
        harness.passBothPriorities();

        Permanent source = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Nebelgast Intruder"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nebelgast Intruder");
        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    private void castIntruder(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NebelgastIntruder()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
