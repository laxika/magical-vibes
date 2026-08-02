package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AzamiLadyOfScrollsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself as the only Wizard and draws a card")
    void tapsItselfAndDraws() {
        Permanent azami = addCreatureReady(player1, new AzamiLadyOfScrolls());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(azami);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(azami.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can tap another Wizard instead of itself as the cost")
    void tapsAnotherWizard() {
        Permanent azami = addCreatureReady(player1, new AzamiLadyOfScrolls());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(azami);
        harness.activateAbility(player1, idx, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        assertThat(azami.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate with no untapped Wizard to tap")
    void cannotActivateWithoutUntappedWizard() {
        Permanent azami = addCreatureReady(player1, new AzamiLadyOfScrolls());
        azami.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(azami);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
