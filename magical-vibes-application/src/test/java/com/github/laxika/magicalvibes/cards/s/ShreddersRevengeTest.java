package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddersRevenge.class, Forest.class, GrizzlyBears.class})
class ShreddersRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Discard mode makes the target player discard two cards")
    void discardMode() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        castShreddersRevenge(0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draw mode makes the target player draw two cards and lose 2 life")
    void drawMode() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        castShreddersRevenge(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Both modes require a player target")
    void modesRejectPermanentTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddersRevenge()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShreddersRevenge(int mode) {
        harness.setHand(player1, List.of(new ShreddersRevenge()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, mode, List.of(player2.getId()));
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
