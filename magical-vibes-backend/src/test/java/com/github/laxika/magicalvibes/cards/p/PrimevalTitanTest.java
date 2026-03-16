package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimevalTitanTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Primeval Titan has MayEffect wrapping SearchLibraryForCardTypesToBattlefieldEffect on ETB and attack")
    void hasCorrectEffects() {
        PrimevalTitan card = new PrimevalTitan();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect etbMay = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etbMay.wrapped()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
        SearchLibraryForCardTypesToBattlefieldEffect searchEffect =
                (SearchLibraryForCardTypesToBattlefieldEffect) etbMay.wrapped();
        assertThat(CardPredicateUtils.describeFilter(searchEffect.filter())).isEqualTo("land card");
        assertThat(searchEffect.entersTapped()).isTrue();
        assertThat(searchEffect.maxCount()).isEqualTo(2);

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst()).isInstanceOf(MayEffect.class);
    }

    // ===== ETB trigger =====

    @Nested
    @DisplayName("ETB trigger")
    class ETBTrigger {

        @Test
        @DisplayName("Casting Primeval Titan triggers may ability prompt")
        void etbTriggersMayPrompt() {
            castPrimevalTitan();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may ability and resolving presents land cards from library")
        void acceptingPresentsLandCards() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.librarySearch().cards())
                    .allMatch(c -> c.hasType(CardType.LAND));
            assertThat(gd.interaction.librarySearch().cards()).hasSize(3); // Forest, Island, Plains
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
            gs.handleLibraryCardChosen(gd, player1, 0);

            // Second search should be presented
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

            // Pick second land
            gs.handleLibraryCardChosen(gd, player1, 0);

            // Both lands should be on the battlefield tapped
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
            long tappedLandCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().hasType(CardType.LAND) && p.isTapped())
                    .count();
            assertThat(tappedLandCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("CR 608.2f: Lands enter battlefield simultaneously after all picks, not one at a time")
        void landsEnterSimultaneously() {
            castPrimevalTitan();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

            int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

            // Pick first land — it should NOT be on the battlefield yet (accumulated)
            gs.handleLibraryCardChosen(gd, player1, 0);
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);

            // Pick second land — now BOTH should enter simultaneously
            gs.handleLibraryCardChosen(gd, player1, 0);
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

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
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
            gs.handleLibraryCardChosen(gd, player1, 0);

            // Decline second pick
            gs.handleLibraryCardChosen(gd, player1, -1);

            // Only one land should have entered
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
            assertThat(gd.interaction.awaitingInputType()).isNull();
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
            gs.handleLibraryCardChosen(gd, player1, -1);

            // No lands entered
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Non-land cards are not offered in the search")
        void nonLandCardsExcluded() {
            castPrimevalTitan();
            // Library with only non-land cards
            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

            // No lands to search for, search fails
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no land cards"));
        }
    }

    // ===== Attack trigger =====

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Primeval Titan triggers may ability prompt")
        void attackTriggersMayPrompt() {
            addReadyPrimevalTitan(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
            assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
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
            gs.handleLibraryCardChosen(gd, player1, 0);

            // Pick second land
            gs.handleLibraryCardChosen(gd, player1, 0);

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

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        }
    }

    // ===== Helpers =====

    private void castPrimevalTitan() {
        harness.setHand(player1, List.of(new PrimevalTitan()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyPrimevalTitan(Player player) {
        Permanent perm = new Permanent(new PrimevalTitan());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Island(), new Plains(), new GrizzlyBears()));
    }
}
