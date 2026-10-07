package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThadaAdelAcquisitor.class, Millstone.class, GrizzlyBears.class, Forest.class})
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

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, millstone.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Millstone");
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getId().equals(millstone.getId()));
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

    @Test
    @DisplayName("The controller may fail to find an artifact even when one is present")
    void mayDeclineToFindArtifact() {
        addAttacker(player1);
        Card millstone = new Millstone();
        harness.setLibrary(player2, List.of(millstone, new GrizzlyBears()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(millstone).hasSize(2);
        assertThat(gd.findExiledCard(millstone.getId())).isNull();
    }

    @Test
    @DisplayName("An exiled artifact still requires payment of its mana cost")
    void requiresManaToCastArtifact() {
        addAttacker(player1);
        Card millstone = new Millstone();
        harness.setLibrary(player2, List.of(millstone, new GrizzlyBears()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(millstone.getId())).isNotNull();
    }

    @Test
    @DisplayName("Exile permission does not allow an artifact to be cast during the end step")
    void respectsNormalCastingTiming() {
        addAttacker(player1);
        Card millstone = new Millstone();
        harness.setLibrary(player2, List.of(millstone, new GrizzlyBears()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.findExiledCard(millstone.getId())).isNotNull();
    }

    @Test
    @DisplayName("An unplayed artifact remains exiled but cannot be played on a later turn")
    void permissionExpiresAtEndOfTurn() {
        addAttacker(player1);
        Card millstone = new Millstone();
        harness.setLibrary(player2, List.of(millstone, new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gd.findExiledCard(millstone.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    private Permanent addAttacker(Player player) {
        Permanent thada = harness.addToBattlefieldAndReturn(player, new ThadaAdelAcquisitor());
        thada.setSummoningSick(false);
        thada.setAttacking(true);
        return thada;
    }
}
