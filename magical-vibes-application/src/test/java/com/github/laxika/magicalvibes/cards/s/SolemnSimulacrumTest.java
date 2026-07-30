package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolemnSimulacrumTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB land search")
    class EnterTheBattlefield {

        @Test
        @DisplayName("Accepting the may ability presents only basic land cards from the library")
        void acceptingPresentsBasicLands() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
            harness.passBothPriorities(); // resolve MayEffect → may prompt

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());

            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                    .hasSize(3)
                    .allMatch(c -> c.hasType(CardType.LAND));
        }

        @Test
        @DisplayName("Chosen basic land enters the battlefield tapped")
        void chosenLandEntersTapped() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            assertThat(battlefield).anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
            assertThat(battlefield.stream().filter(p -> p.getCard().hasType(CardType.LAND)).count()).isEqualTo(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Declining the may ability skips the library search")
        void decliningSkipsSearch() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
        }

        @Test
        @DisplayName("Player may fail to find a basic land")
        void mayFailToFind() {
            castSolemnSimulacrum();
            setupLibrary();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().hasType(CardType.LAND));
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("No search prompt when the library holds no basic lands")
        void noBasicLandsInLibrary() {
            castSolemnSimulacrum();
            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        }
    }

    @Nested
    @DisplayName("Death draw")
    class Death {

        @Test
        @DisplayName("Accepting the death may ability draws a card")
        void deathAcceptDraws() {
            harness.addToBattlefield(player1, new SolemnSimulacrum());
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            int handBefore = gd.playerHands.get(player1.getId()).size();

            gs.playCard(gd, player1, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves, Solemn dies → death trigger on stack

            harness.assertInGraveyard(player1, "Solemn Simulacrum");

            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 1);
        }

        @Test
        @DisplayName("Declining the death may ability draws nothing")
        void deathDeclineDrawsNothing() {
            harness.addToBattlefield(player1, new SolemnSimulacrum());
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            int handBefore = gd.playerHands.get(player1.getId()).size();

            gs.playCard(gd, player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
        }
    }

    private void castSolemnSimulacrum() {
        harness.setHand(player1, List.of(new SolemnSimulacrum()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Island(), new Plains(), new GrizzlyBears()));
    }
}
