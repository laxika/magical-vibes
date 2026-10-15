package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({ShreddersRevenge.class, Forest.class, FootNinjas.class})
class ShreddersRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Discard mode makes the target player discard two cards")
    void discardMode() {
        harness.setHand(player2, List.of(new Forest(), new FootNinjas(), new Forest()));
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
        harness.setLibrary(player2, List.of(new FootNinjas(), new Forest()));
        castShreddersRevenge(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Both modes require a player target")
    void modesRejectPermanentTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FootNinjas());
        harness.setHand(player1, List.of(new ShreddersRevenge()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard mode discards the only card in a short hand")
    void discardModeWithOneCard() {
        harness.setHand(player2, List.of(new Forest()));
        castShreddersRevenge(0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discard mode resolves harmlessly against an empty hand")
    void discardModeWithEmptyHand() {
        harness.setHand(player2, List.of());
        castShreddersRevenge(0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Draw mode can target its caster and only that player loses life")
    void drawModeTargetsCaster() {
        harness.setHand(player1, List.of(new ShreddersRevenge()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new FootNinjas()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discard mode can target its caster and the caster chooses the discards")
    void discardModeTargetsCaster() {
        harness.setHand(player1, List.of(new ShreddersRevenge(), new Forest(), new FootNinjas(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        addManaForSpell();
        harness.castModalSorcery(player1, 0, 0, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Foot Ninjas");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
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
