package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JadeGuardian;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrownerOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself as the only Merfolk and target player mills a card")
    void tapsItselfAndTargetMills() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());

        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player2.getId());
        harness.passBothPriorities();

        assertThat(drowner.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 1);
    }

    @Test
    @DisplayName("Can tap another Merfolk instead of itself as the cost")
    void tapsAnotherMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        Permanent merfolk = addCreatureReady(player1, new JadeGuardian());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player2.getId());

        // Two valid Merfolk -> choose which to tap
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(drowner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        harness.activateAbility(player1, idx, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate with no untapped Merfolk to tap")
    void cannotActivateWithoutUntappedMerfolk() {
        Permanent drowner = addCreatureReady(player1, new DrownerOfSecrets());
        drowner.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
