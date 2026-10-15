package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedguideAsh.class, Forest.class, Island.class, GrizzlyBears.class, WrathOfGod.class})
class SeedguideAshTest extends BaseCardTest {

    @Nested
    @DisplayName("Death trigger")
    @CardUsed({SeedguideAsh.class, Forest.class, Island.class, GrizzlyBears.class, WrathOfGod.class})
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

            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);

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
        void mayChooseZeroForestsEvenWhenAvailable() {
            setupLibrary();
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, -1);

            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void mayStopAfterOneForest() {
            setupLibrary();
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, -1);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(1).allSatisfy(p -> {
                        assertThat(p.getCard()).isInstanceOf(Forest.class);
                        assertThat(p.isTapped()).isTrue();
                    });
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void searchCompletesWhenOnlyTwoForestsExist() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(2).allSatisfy(p -> assertThat(p.isTapped()).isTrue());
            assertThat(gd.playerDecks.get(player1.getId()))
                    .hasSize(1).allMatch(c -> c instanceof Island);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void cannotFindMoreThanThreeForests() {
            harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .hasSize(3).allSatisfy(p -> assertThat(p.isTapped()).isTrue());
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void acceptingWithEmptyLibraryCompletesAbility() {
            harness.setLibrary(player1, List.of());
            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("No Forest cards in library means the search fails to find")
        void noForestsFailsToFind() {
            harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));

            killSeedguideAsh();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void killSeedguideAsh() {
        harness.addToBattlefield(player1, new SeedguideAsh());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0); // resolve Wrath â€” Seedguide Ash dies
        harness.passBothPriorities(); // resolve death MayEffect â†’ may prompt
    }
}
