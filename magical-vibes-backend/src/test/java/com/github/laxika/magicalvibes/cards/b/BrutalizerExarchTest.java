package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BrutalizerExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Brutalizer Exarch has a ChooseOneEffect with two ETB options")
    void hasCorrectEffects() {
        BrutalizerExarch card = new BrutalizerExarch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.options()).hasSize(2);
        assertThat(effect.options().get(0).effect()).isInstanceOf(SearchLibraryForCreatureToTopOfLibraryEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(PutTargetOnBottomOfLibraryEffect.class);
    }

    @Nested
    @DisplayName("Mode 1: Search library for creature to top")
    class SearchMode {

        @Test
        @DisplayName("Choosing mode 1 triggers library search for creature cards")
        void mode1TriggersLibrarySearch() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Brutalizer Exarch"));
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            // Only creature cards should be shown
            assertThat(gd.interaction.librarySearch().cards())
                    .allMatch(c -> c.hasType(CardType.CREATURE));
        }

        @Test
        @DisplayName("Choosing a creature puts it on top of library")
        void choosingCreaturePutsOnTop() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            List<Card> offered = gd.interaction.librarySearch().cards();
            String chosenName = offered.getFirst().getName();

            gs.handleLibraryCardChosen(gd, player1, 0);

            // The chosen card should be on top of the library
            List<Card> deck = gd.playerDecks.get(player1.getId());
            assertThat(deck).isNotEmpty();
            assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        }

        @Test
        @DisplayName("Failing to find is allowed")
        void failToFindIsAllowed() {
            setupLibraryWithCreatures();
            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            // -1 means fail to find
            gs.handleLibraryCardChosen(gd, player1, -1);

            // Deck should be shuffled but same size (no card moved)
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Only creature cards are shown in library search")
        void onlyCreaturesShown() {
            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(new Plains(), new Island(), new Forest()));

            castWithMode1();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // No creatures in library, so search finds nothing
            GameData gd = harness.getGameData();
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        }
    }

    @Nested
    @DisplayName("Mode 2: Put noncreature permanent on bottom")
    class BottomMode {

        @Test
        @DisplayName("Choosing mode 2 puts target noncreature permanent on bottom of owner's library")
        void mode2PutsNoncreaturePermanentOnBottom() {
            // Put a noncreature permanent (enchantment or artifact) onto the battlefield
            harness.addToBattlefield(player2, new Plains());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            // The Plains should no longer be on the battlefield
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Plains"));
            // It should be on the bottom of the owner's library
            List<Card> deck = gd.playerDecks.get(player2.getId());
            assertThat(deck.getLast().getName()).isEqualTo("Plains");
        }

        @Test
        @DisplayName("Mode 2 works on artifacts")
        void mode2WorksOnArtifacts() {
            harness.addToBattlefield(player2, new BarbedBattlegear());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Barbed Battlegear"));
        }

        @Test
        @DisplayName("Brutalizer Exarch enters the battlefield with mode 2")
        void exarchEntersBattlefield() {
            harness.addToBattlefield(player2, new Plains());
            UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

            castWithMode2(targetId);
            harness.passBothPriorities(); // resolve creature

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Brutalizer Exarch"));
        }
    }

    private void castWithMode1() {
        harness.setHand(player1, List.of(new BrutalizerExarch()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 0); // mode 0 = search library
    }

    private void castWithMode2(UUID targetId) {
        harness.setHand(player1, List.of(new BrutalizerExarch()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 1, targetId); // mode 1 = put on bottom
    }

    private void setupLibraryWithCreatures() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Plains(), new Island(), new Forest()));
    }
}
