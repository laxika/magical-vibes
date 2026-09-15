package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PufferExtract.class, FreshVolunteers.class})
class PufferExtractTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target creature you control +X/+X until end of turn")
    void boostsTargetCreatureByPaidX() {
        Permanent extract = harness.addToBattlefieldAndReturn(player1, new PufferExtract());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, volunteers.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, volunteers)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(5);
        assertThat(extract.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows X to be zero while still scheduling destruction")
    void zeroXStillDestroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefieldAndReturn(player1, new PufferExtract());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        harness.activateAbility(player1, 0, 0, volunteers.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, volunteers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Destroys the target creature at the beginning of the next end step")
    void destroysTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefieldAndReturn(player1, new PufferExtract());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, volunteers.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fresh Volunteers");

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new PufferExtract());
        Permanent opponentVolunteers = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, opponentVolunteers.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent extract = harness.addToBattlefieldAndReturn(player1, new PufferExtract());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, extract.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
