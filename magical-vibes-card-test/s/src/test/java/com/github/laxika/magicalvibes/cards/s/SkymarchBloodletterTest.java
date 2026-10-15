package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkymarchBloodletter.class, LightningStrike.class})
class SkymarchBloodletterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castSkymarchBloodletter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skymarch Bloodletter");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger makes target opponent lose 1 life and controller gain 1 life")
    void etbDrainsLife() {
        castSkymarchBloodletter();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 5);

        castSkymarchBloodletter();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castSkymarchBloodletter();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast by targeting yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new SkymarchBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("ETB drain still resolves after the source creature is destroyed")
    void drainResolvesAfterSourceLeavesBattlefield() {
        castSkymarchBloodletter();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, findPermanent(player1, "Skymarch Bloodletter").getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skymarch Bloodletter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The entering creature's controller gains life and their opponent loses life")
    void opponentControlledBloodletterDrainsOtherPlayer() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SkymarchBloodletter()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    private void castSkymarchBloodletter() {
        harness.setHand(player1, List.of(new SkymarchBloodletter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, player2.getId());
    }
}
