package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimevalTitan.class, Forest.class, Island.class, Plains.class, RuneclawBear.class, TerramorphicExpanse.class})
class PrimevalTitanTest extends BaseCardTest {


    @CardUsed({PrimevalTitan.class, Forest.class, Island.class, Plains.class, RuneclawBear.class, TerramorphicExpanse.class})
    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("Casting Primeval Titan triggers may ability prompt")
        void etbTriggersMayPrompt() {
            castPrimevalTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may ability and resolving presents land cards from library")
        void acceptingPresentsLandCards() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .allMatch(c -> c.hasType(CardType.LAND));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3); // Forest, Island, Plains
        }

        @Test
        @DisplayName("Picking two lands puts both onto battlefield tapped")
        void pickingTwoLandsPutsBothOnBattlefieldTapped() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Pick first land
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // Second search should be presented
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

            // Pick second land
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // Both lands should be on the battlefield tapped
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
            long tappedLandCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                    .count();
            assertThat(tappedLandCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Lands enter battlefield simultaneously after all picks, not one at a time")
        void landsEnterSimultaneously() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Pick first land — it should NOT be on the battlefield yet (accumulated)
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);

            // Pick second land — now BOTH should enter simultaneously
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        }

        @Test
        @DisplayName("Declining may ability skips the library search")
        void decliningMaySkipsSearch() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }

        @Test
        @DisplayName("Can fail to find after picking first land")
        void canFailToFindAfterFirstPick() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Pick first land
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // Decline second pick
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

            // Only one land should have entered
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Can fail to find on first pick (pick zero lands)")
        void canFailToFindOnFirstPick() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Decline first pick
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

            // No lands entered
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Can find two nonbasic lands with the same name")
        void findsDuplicateNonbasicLands() {
            castPrimevalTitan();
            TerramorphicExpanse first = new TerramorphicExpanse();
            TerramorphicExpanse second = new TerramorphicExpanse();
            harness.setLibrary(player1, List.of(first, second, new RuneclawBear()));
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getId().equals(first.getId())
                            || p.getCard().getId().equals(second.getId()))
                    .hasSize(2)
                    .allMatch(Permanent::isTapped);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("A library with only one land completes the search after that pick")
        void onlyOneLandAvailable() {
            castPrimevalTitan();
            Forest forest = new Forest();
            harness.setLibrary(player1, List.of(forest, new RuneclawBear()));
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getId().equals(forest.getId()))
                    .hasSize(1)
                    .allMatch(Permanent::isTapped);
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Accepting the ability with an empty library completes without a choice")
        void emptyLibraryCompletesSearch() {
            castPrimevalTitan();
            harness.setLibrary(player1, List.of());
            harness.passBothPriorities();
            harness.passBothPriorities();
            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Non-land cards are not offered in the search")
        void nonLandCardsExcluded() {
            castPrimevalTitan();
            // Library with only non-land cards
            harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));

            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

            // No lands to search for, search fails
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no land cards"));
        }
    }


    @CardUsed({PrimevalTitan.class, Forest.class, Island.class, Plains.class, RuneclawBear.class, TerramorphicExpanse.class})
    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Primeval Titan triggers may ability prompt")
        void attackTriggersMayPrompt() {
            addReadyPrimevalTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting attack may and picking two lands puts both onto battlefield tapped")
        void attackPickingTwoLands() {
            addReadyPrimevalTitan(player1);
            setupLibrary();

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Pick first land
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            // Pick second land
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
            long tappedLandCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                    .count();
            assertThat(tappedLandCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Declining attack may ability skips the library search")
        void decliningAttackMaySkipsSearch() {
            addReadyPrimevalTitan(player1);
            setupLibrary();

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }
    }


    private void castPrimevalTitan() {
        harness.castFromHand(player1, new PrimevalTitan(), "{4}{G}{G}");
    }

    private Permanent addReadyPrimevalTitan(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PrimevalTitan());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new RuneclawBear()));
    }
}
