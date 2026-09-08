package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThadaAdelAcquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage searches the damaged player's library for an artifact")
    void searchesForArtifact() {
        addAttacker(player1);
        harness.setLibrary(player2, List.of(new Millstone(), new GrizzlyBears(), new Forest()));

        resolveCombat();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Millstone");
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("The chosen artifact is exiled face up and may be played this turn")
    void exilesArtifactWithPlayPermission() {
        addAttacker(player1);
        Card millstone = new Millstone();
        harness.setLibrary(player2, List.of(millstone, new GrizzlyBears()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.findExiledCard(millstone.getId()).faceDown()).isFalse();
        assertThat(gd.exilePlayPermissions.get(millstone.getId())).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCardFromExile(gd, player1, millstone.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Millstone");
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(millstone.getId()));
    }

    @Test
    @DisplayName("No artifact in the damaged player's library does nothing")
    void noArtifactFound() {
        addAttacker(player1);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    private Permanent addAttacker(Player player) {
        Permanent thada = new Permanent(new ThadaAdelAcquisitor());
        thada.setSummoningSick(false);
        thada.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(thada);
        return thada;
    }
}
