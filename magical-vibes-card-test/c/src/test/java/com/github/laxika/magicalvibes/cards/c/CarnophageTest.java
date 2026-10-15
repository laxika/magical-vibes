package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Seizures;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Carnophage.class, Seizures.class})
class CarnophageTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life during upkeep keeps Carnophage untapped")
    void payingLifeKeepsCarnophageUntapped() {
        Permanent carnophage = addCarnophage();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(carnophage.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Declining to pay 1 life during upkeep taps Carnophage")
    void decliningLifePaymentTapsCarnophage() {
        Permanent carnophage = addCarnophage();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(carnophage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Carnophage does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent carnophage = addCarnophage();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(carnophage.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addCarnophage() {
        return harness.addToBattlefieldAndReturn(player1, new Carnophage());
    }

    @Test
    @DisplayName("Paying life does not untap an already tapped Carnophage")
    void payingLifeDoesNotUntapCarnophage() {
        Permanent carnophage = addCarnophage();
        advanceToUpkeep(player1);
        carnophage.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(carnophage.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An already tapped Carnophage still offers payment and allows declining")
    void tappedCarnophageCanDeclinePayment() {
        Permanent carnophage = addCarnophage();
        advanceToUpkeep(player1);
        carnophage.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(carnophage.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Each Carnophage controller pays life only for their own creature")
    void paymentUsesCurrentController() {
        Permanent first = addCarnophage();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Carnophage());
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(firstLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(secondLife - 1);
    }

    @Test
    @CardUsed({Carnophage.class, Seizures.class})
    @DisplayName("Declining upkeep payment triggers an attached Seizures")
    void decliningPaymentTriggersAttachedAura() {
        Permanent carnophage = addCarnophage();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Seizures());
        aura.setAttachedTo(carnophage.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(carnophage.isTapped()).isTrue();

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }
}
