package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterTribeCaptain.class, GrizzlyBears.class})
class WaterTribeCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Ability boosts creatures you control until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, newly entered captain can activate repeatedly and the boosts stack")
    void repeatedActivationsStackWithoutTapCost() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());
        captain.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost includes creatures entering before resolution but not after resolution")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());
        Permanent otherCaptain = harness.addToBattlefieldAndReturn(player1, new WaterTribeCaptain());
        Permanent opponentCaptain = harness.addToBattlefieldAndReturn(player2, new WaterTribeCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, captain));

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherCaptain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCaptain)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCaptain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCaptain)).isEqualTo(3);
    }
}
