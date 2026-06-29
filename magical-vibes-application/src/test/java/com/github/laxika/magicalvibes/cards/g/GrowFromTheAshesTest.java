package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowFromTheAshesTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has KickerEffect with cost {2}")
    void hasKickerEffect() {
        GrowFromTheAshes card = new GrowFromTheAshes();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{2}"));
    }

    @Test
    @DisplayName("Has KickerReplacementEffect with search-for-basic-land effects")
    void hasCorrectSpellEffects() {
        GrowFromTheAshes card = new GrowFromTheAshes();

        assertThat(card.getEffects(EffectSlot.SPELL))
                .hasSize(1)
                .anySatisfy(e -> {
                    assertThat(e).isInstanceOf(KickerReplacementEffect.class);
                    KickerReplacementEffect kre = (KickerReplacementEffect) e;
                    assertThat(kre.baseEffect()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
                    SearchLibraryForCardTypesToBattlefieldEffect base =
                            (SearchLibraryForCardTypesToBattlefieldEffect) kre.baseEffect();
                    assertThat(base.entersTapped()).isFalse();
                    assertThat(base.maxCount()).isEqualTo(1);

                    assertThat(kre.kickedEffect()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
                    SearchLibraryForCardTypesToBattlefieldEffect kicked =
                            (SearchLibraryForCardTypesToBattlefieldEffect) kre.kickedEffect();
                    assertThat(kicked.entersTapped()).isFalse();
                    assertThat(kicked.maxCount()).isEqualTo(2);
                });
    }

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        setupAndCast();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Grow from the Ashes");
    }

    @Test
    @DisplayName("Without kicker — presents basic lands with destination battlefield (untapped)")
    void resolvingPresentsBasicLandsToBattlefield() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Without kicker — chosen basic land enters the battlefield untapped")
    void chosenBasicLandEntersUntapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped());
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Without kicker — player can fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("With kicker — initiates multi-pick search for two basic lands")
    void kickedSearchesForTwoBasicLands() {
        setupAndCastKicked();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("With kicker — two basic lands enter the battlefield untapped")
    void kickedPutsTwoLandsOntoBattlefield() {
        setupAndCastKicked();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Choose first land
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        // Choose second land
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        long untappedLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped())
                .count();
        assertThat(untappedLands).isGreaterThanOrEqualTo(2);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Resolving with no basic lands in library does not prompt for library choice")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrowFromTheAshes(), new GrowFromTheAshes()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Resolving with empty library does not prompt for library choice")
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
        harness.setHand(player1, List.of(new GrowFromTheAshes()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupAndCastKicked() {
        harness.setHand(player1, List.of(new GrowFromTheAshes()));
        harness.addMana(player1, ManaColor.GREEN, 5); // {2}{G} + kicker {2}
        harness.castKickedSorcery(player1, 0, null);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new Mountain()));
    }
}
