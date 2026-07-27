package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheDawningArchaicTest extends BaseCardTest {

    private void addReadyAttacker() {
        Permanent archaic = new Permanent(new TheDawningArchaic());
        archaic.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(archaic);
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Nested
    @DisplayName("Cost reduction")
    class CostReduction {

        @Test
        @DisplayName("Costs the full {10} with an empty graveyard")
        void fullCostWithEmptyGraveyard() {
            harness.setHand(player1, List.of(new TheDawningArchaic()));
            harness.addMana(player1, ManaColor.COLORLESS, 9);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Costs {1} less for each instant and sorcery card in your graveyard")
        void reducedByInstantsAndSorceries() {
            harness.setGraveyard(player1, List.of(new Shock(), new CounselOfTheSoratami(), new Shock()));
            harness.setHand(player1, List.of(new TheDawningArchaic()));
            // 2 instants + 1 sorcery = {10} - 3 = 7 mana
            harness.addMana(player1, ManaColor.COLORLESS, 7);

            harness.castCreature(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("The Dawning Archaic");
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Creature cards in your graveyard do not reduce the cost")
        void creatureCardsDoNotReduce() {
            harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
            harness.setHand(player1, List.of(new TheDawningArchaic()));
            harness.addMana(player1, ManaColor.COLORLESS, 9);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }

        @Test
        @DisplayName("Instants in an opponent's graveyard do not reduce the cost")
        void opponentGraveyardDoesNotReduce() {
            harness.setGraveyard(player2, List.of(new Shock(), new Shock()));
            harness.setHand(player1, List.of(new TheDawningArchaic()));
            harness.addMana(player1, ManaColor.COLORLESS, 9);

            assertThatThrownBy(() -> harness.castCreature(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not playable");
        }
    }

    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Only instant and sorcery cards from your own graveyard are legal targets")
        void onlyOwnInstantsAndSorceriesAreTargetable() {
            addReadyAttacker();
            Card ownShock = new Shock();
            Card ownBears = new GrizzlyBears();
            Card opponentShock = new Shock();
            harness.setGraveyard(player1, new ArrayList<>(List.of(ownShock, ownBears)));
            harness.setGraveyard(player2, new ArrayList<>(List.of(opponentShock)));

            declareAttack();

            var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactly(ownShock.getId());
        }

        @Test
        @DisplayName("No instant or sorcery in your graveyard produces no target choice")
        void noValidTargetSkipsTrigger() {
            addReadyAttacker();
            harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
            harness.setGraveyard(player2, new ArrayList<>(List.of(new Shock())));

            declareAttack();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        }

        @Test
        @DisplayName("Casts the chosen sorcery for free and exiles it instead of returning it to the graveyard")
        void castsNonTargetedSorceryForFreeAndExilesIt() {
            addReadyAttacker();
            Card counsel = new CounselOfTheSoratami();
            harness.setGraveyard(player1, new ArrayList<>(List.of(counsel)));
            int handSizeBefore = gd.playerHands.get(player1.getId()).size();

            declareAttack();

            harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
            harness.passBothPriorities(); // resolve attack trigger → queues the may-cast
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities(); // resolve Counsel of the Soratami

            // Cast without paying its mana cost — no mana was ever added.
            assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getId().equals(counsel.getId()));
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getId().equals(counsel.getId()));
        }

        @Test
        @DisplayName("A targeted instant cast this way is exiled after it resolves")
        void castsTargetedInstantAndExilesIt() {
            addReadyAttacker();
            Card shock = new Shock();
            harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            declareAttack();

            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearsId);
            harness.passBothPriorities(); // resolve Shock

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getId().equals(shock.getId()));
        }

        @Test
        @DisplayName("Declining the may-cast leaves the card in your graveyard")
        void decliningLeavesCardInGraveyard() {
            addReadyAttacker();
            Card shock = new Shock();
            harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));

            declareAttack();

            harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getId().equals(shock.getId()));
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .noneMatch(c -> c.getId().equals(shock.getId()));
        }
    }
}
