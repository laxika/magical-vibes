package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.Artillerize;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PraetorsGrasp.class, GrizzlyBears.class, Shock.class, Swamp.class, Artillerize.class})
class PraetorsGraspTest extends BaseCardTest {


    @Test
    @DisplayName("Presents library search showing all cards in opponent's library")
    void presentsLibrarySearchWithAllCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(bears, shock, swamp));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        // All cards should be shown, not filtered by type
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
    }


    @Test
    @DisplayName("Chosen card is exiled face down with ownership preserved and caster play permission")
    void chosenCardIsExiledWithPlayPermission() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Choose the card
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Ownership is preserved; only the searching player can inspect the face-down card.
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(bears.getId()).exilerId()).isEqualTo(player1.getId());

        // Play permission should be granted to caster
        assertThat(gd.exilePlayPermissions.get(bears.getId()))
                .isEqualTo(player1.getId());

        // Card should not be in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Opponent's library is shuffled after search")
    void libraryIsShuffledAfterSearch() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Log should mention shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffled") || log.contains("Library is shuffled"));
    }


    @Test
    @DisplayName("Empty opponent library skips search")
    void emptyLibrarySkipsSearch() {
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }


    @Test
    @DisplayName("Praetor's Grasp goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setLibrary(player2, List.of());

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Praetor's Grasp");
    }


    @Test
    @DisplayName("Caster can play exiled card from exile")
    void canPlayExiledCard() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Now player1 should be able to cast the exiled Grizzly Bears
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCardFromExile(gd, player1, bears.getId(), null, null);
        harness.passBothPriorities();

        // Bears should be on player1's battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Play permission should be removed
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());

        // Card should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    void unrestrictedSearchCannotBeDeclined() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));
        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1))).isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.findExiledCard(swamp.getId())).isNotNull();
    }

    @Test
    void castingRequiresTheCardsColoredManaAndFailedAttemptKeepsPermission() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, bears.getId(), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCardFromExile(gd, player1, bears.getId(), null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canInitiateCastingAnExiledSpellWithAnAdditionalSacrificeCost() {
        Card artillerize = new Artillerize();
        harness.setLibrary(player2, List.of(artillerize));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatCode(() -> gs.playCardFromExile(gd, player1, artillerize.getId(), null, player2.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Exiled card is removed from exile when played")
    void exiledCardRemovedFromExile() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Play the exiled land
        gs.playCardFromExile(gd, player1, swamp.getId(), null, null);

        // Land should be on player1's battlefield
        harness.assertOnBattlefield(player1, "Swamp");

        // Should count as a land play
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
