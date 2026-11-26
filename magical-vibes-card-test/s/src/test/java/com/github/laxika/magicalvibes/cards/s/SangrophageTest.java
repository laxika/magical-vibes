package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sangrophage.class})
class SangrophageTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life during upkeep keeps Sangrophage untapped")
    void payingLifeKeepsSangrophageUntapped() {
        Permanent sangrophage = addSangrophage();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sangrophage.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Declining to pay 2 life during upkeep taps Sangrophage")
    void decliningLifePaymentTapsSangrophage() {
        Permanent sangrophage = addSangrophage();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sangrophage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At 1 life its controller can't pay 2 life, isn't asked, and Sangrophage is tapped")
    void insufficientLifeTapsSangrophage() {
        giveManaSource(player1);
        giveManaSource(player2);
        Permanent sangrophage = addSangrophage();
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class))
                .isNull();

        assertThat(sangrophage.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Sangrophage does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent sangrophage = addSangrophage();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(sangrophage.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private Permanent addSangrophage() {
        return harness.addToBattlefieldAndReturn(player1, new Sangrophage());
    }

    @Test
    @DisplayName("Paying life does not untap an already tapped Sangrophage")
    void payingLifeDoesNotUntapSangrophage() {
        Permanent sangrophage = addSangrophage();
        advanceToUpkeep(player1);
        sangrophage.tap();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sangrophage.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The controller pays life for Sangrophage on their own upkeep")
    void secondPlayerPaysForTheirSangrophage() {
        Permanent sangrophage = harness.addToBattlefieldAndReturn(player2, new Sangrophage());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(sangrophage.isTapped()).isFalse();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }
}
