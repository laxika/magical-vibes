package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysticShield.class, PygmyRazorback.class})
class RhysticShieldTest extends BaseCardTest {

    @Test
    void addsBothToughnessBonusesWhenNoPlayerPays() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PygmyRazorback());

        castShield();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void anyPlayerCanPayToPreventAdditionalBonus() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        castShield();
        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void currentPlayerCanPayBeforeOtherPlayersAreAsked() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        castShield();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGetEitherBonus() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        castShield();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(lateCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void bonusesWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());

        castShield();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
    }

    private void castShield() {
        harness.castFromHand(player1, new RhysticShield(), "{1}{W}");
        harness.passBothPriorities();
    }
}
