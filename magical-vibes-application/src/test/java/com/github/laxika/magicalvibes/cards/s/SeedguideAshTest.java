package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeedguideAshTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    class DeathTrigger {

        @Test
        @DisplayName("Dying triggers the may ability prompt")
        void dyingTriggersMayPrompt() {
            killSeedguideAsh();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting presents only Forest cards from the library")
        void acceptingPresentsForestCards() {
            setupLibrary();
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.getName().equals("Forest"))
                    .hasSize(3);
        }

        @Test
        @DisplayName("Picking three Forests puts them all onto the battlefield tapped")
        void pickingThreeForestsPutsThemOnBattlefieldTapped() {
            setupLibrary();
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);

            int before = gd.playerBattlefields.get(player1.getId()).size();

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before + 3);
            long tappedForests = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Forest") && p.isTapped())
                    .count();
            assertThat(tappedForests).isEqualTo(3);
        }

        @Test
        @DisplayName("Declining the may ability skips the library search")
        void decliningSkipsSearch() {
            setupLibrary();
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }

        @Test
        @DisplayName("No Forest cards in library means the search fails to find")
        void noForestsFailsToFind() {
            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(new Island(), new GrizzlyBears()));

            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }
    }

    // ===== Helpers =====

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Forest(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void killSeedguideAsh() {
        harness.addToBattlefield(player1, new SeedguideAsh());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — Seedguide Ash dies
        harness.passBothPriorities(); // resolve death MayEffect → may prompt
    }
}
