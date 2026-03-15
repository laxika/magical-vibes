package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaravanVigilTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has MorbidReplacementEffect wrapping search-to-hand base and search-to-battlefield morbid")
    void hasCorrectStructure() {
        CaravanVigil card = new CaravanVigil();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(MorbidReplacementEffect.class);

        MorbidReplacementEffect effect =
                (MorbidReplacementEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.baseEffect()).isInstanceOf(SearchLibraryForBasicLandToHandEffect.class);
        assertThat(effect.morbidEffect()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);

        SearchLibraryForCardTypesToBattlefieldEffect morbidEffect =
                (SearchLibraryForCardTypesToBattlefieldEffect) effect.morbidEffect();
        assertThat(morbidEffect.requiresBasicSupertype()).isTrue();
        assertThat(morbidEffect.entersTapped()).isFalse();
    }

    // ===== Without morbid — basic land goes to hand =====

    @Test
    @DisplayName("Without morbid, resolving presents basic land search to hand")
    void withoutMorbidPresentsSearchToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Without morbid, chosen basic land goes to hand")
    void withoutMorbidLandGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.librarySearch().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== With morbid — basic land goes to battlefield untapped =====

    @Test
    @DisplayName("With morbid, resolving presents basic land search to battlefield")
    void withMorbidPresentsSearchToBattlefield() {
        setupAndCast();
        setupLibrary();
        enableMorbid();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("With morbid, chosen basic land enters the battlefield untapped")
    void withMorbidLandEntersBattlefieldUntapped() {
        setupAndCast();
        setupLibrary();
        enableMorbid();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped());
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Morbid checked at resolution time =====

    @Test
    @DisplayName("Morbid is checked at resolution time, not cast time")
    void morbidCheckedAtResolution() {
        setupAndCast();
        setupLibrary();

        // No creature has died when casting — enable morbid after casting
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        // Should use morbid path (battlefield) since morbid is met at resolution
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Player can fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Empty library does not prompt for search")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CaravanVigil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void enableMorbid() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
    }
}
