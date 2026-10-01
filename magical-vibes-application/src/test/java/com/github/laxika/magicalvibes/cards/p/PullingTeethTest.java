package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullingTeeth.class, MothdustChangeling.class, Mutavault.class})
class PullingTeethTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {1}{B}
        harness.setHand(player2, List.of(
                new MothdustChangeling(), new MothdustChangeling(), new MothdustChangeling()));
    }

    @Test
    @DisplayName("Winning the clash makes the target player discard two cards")
    void winningDiscardsTwo() {
        prepare();
        // Caster reveals Mothdust Changeling (MV 1), opponent reveals Mutavault (MV 0) → caster wins.
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Won clash: an extra discard is queued on top of the guaranteed one — two discards total.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Losing the clash makes the target player discard only one card")
    void losingDiscardsOne() {
        prepare();
        // Both reveal Mutavault (MV 0) → tie, caster does not win.
        harness.setLibrary(player1, List.of(new Mutavault(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Winning the clash makes the caster discard when the caster is the target player")
    void winningDiscardsFromTheChosenTargetPlayer() {
        harness.setHand(player1, List.of(
                new PullingTeeth(), new MothdustChangeling(), new MothdustChangeling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player with an empty hand discards nothing even on a won clash")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
