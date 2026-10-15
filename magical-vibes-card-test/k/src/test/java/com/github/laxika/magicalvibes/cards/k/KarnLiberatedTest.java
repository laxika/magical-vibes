package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.Batterskull;
import com.github.laxika.magicalvibes.cards.m.MutagenicGrowth;
import com.github.laxika.magicalvibes.cards.n.NumbingDose;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({KarnLiberated.class, GrizzlyBears.class, Forest.class})
class KarnLiberatedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KarnLiberated()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Karn Liberated");
    }

    @Test
    @DisplayName("Resolving puts Karn on battlefield with loyalty 6")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new KarnLiberated()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent karn = findPermanent(player1, "Karn Liberated");
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(karn.isSummoningSick()).isFalse();
    }

    @Nested
    @DisplayName("+4 ability")
    @CardUsed({KarnLiberated.class, GrizzlyBears.class, Forest.class})
    class PlusFourAbility {

        @Test
        @DisplayName("+4 increases loyalty and prompts target to exile from hand")
        void plusFourIncreasesLoyaltyAndPrompts() {
            Permanent karn = addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(10); // 6 + 4
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
            assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Target player exiles a card of their choice")
        void targetExilesCardOfChoice() {
            addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            // Target player (player2) chooses which card to exile
            harness.handleCardChosen(player2, 0); // exile Grizzly Bears

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");

            // Card should be in exile, not graveyard
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            harness.assertNotInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Exiled card is tracked with Karn's permanent")
        void exiledCardTrackedWithKarn() {
            Permanent karn = addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player2, 0);

            // Card should be tracked in permanentExiledCards with Karn's permanent ID
            List<com.github.laxika.magicalvibes.model.Card> karnExiled = gd.getCardsExiledByPermanent(karn.getId());
            assertThat(karnExiled).isNotNull();
            assertThat(karnExiled).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Can target self to exile from own hand")
        void canTargetSelf() {
            addReadyKarn(player1);
            harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
            assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

            harness.handleCardChosen(player1, 1); // exile Forest

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
        }

        @Test
        @DisplayName("Target with empty hand results in no prompt")
        void targetWithEmptyHand() {
            addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>());

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            // No exile prompt since hand is empty
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to exile"));
        }

        @Test
        @DisplayName("Caster cannot make the exile choice for the target")
        void casterCannotChooseForTarget() {
            addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not your turn to choose");
        }
    }

    @Nested
    @DisplayName("−3 ability")
    @CardUsed({KarnLiberated.class, GrizzlyBears.class})
    class MinusThreeAbility {

        @Test
        @DisplayName("−3 exiles target permanent and decreases loyalty")
        void minusThreeExilesTargetPermanent() {
            Permanent karn = addReadyKarn(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");

            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 6 - 3
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Exiled permanent is tracked with Karn's permanent")
        void exiledPermanentTrackedWithKarn() {
            Permanent karn = addReadyKarn(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");

            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            List<com.github.laxika.magicalvibes.model.Card> karnExiled = gd.getCardsExiledByPermanent(karn.getId());
            assertThat(karnExiled).isNotNull();
            assertThat(karnExiled).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Can exile own permanent")
        void canExileOwnPermanent() {
            addReadyKarn(player1);
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent bears = findPermanent(player1, "Grizzly Bears");

            // Karn is at index 0, bears at index 1
            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot activate with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent karn = addReadyKarn(player1);
            karn.setCounterCount(CounterType.LOYALTY, 2); // Less than 3
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty");
        }
    }

    @Nested
    @DisplayName("−14 ability")
    @CardUsed({KarnLiberated.class, GrizzlyBears.class, Forest.class})
    class MinusFourteenAbility {

        @Test
        @DisplayName("−14 restarts the game and puts exiled cards onto battlefield")
        void restartGamePutsExiledCardsOntoBattlefield() {
            Permanent karn = addReadyKarn(player1);
            karn.setCounterCount(CounterType.LOYALTY, 14);

            // Exile a creature with Karn's -3 first
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");
            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            // Verify bear is tracked with Karn
            assertThat(gd.getCardsExiledByPermanent(karn.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Reset loyalty ability flag and set loyalty to 14
            karn.setLoyaltyActivationsThisTurn(0);
            karn.setCounterCount(CounterType.LOYALTY, 14);

            // Activate -14 (ultimate)
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            // Game enters mulligan phase after restart.
            assertThat(gd.status).isEqualTo(GameStatus.MULLIGAN);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(7);

            // Complete mulligans — Karn's exiled cards enter battlefield
            harness.skipMulligan();

            // Grizzly Bears should be on player1's battlefield (controller of Karn)
            harness.assertOnBattlefield(player1, "Grizzly Bears");

            // ...and must have LEFT exile — a card can only exist in one zone
            assertThat(gd.getPlayerExiledCards(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));

            // Controller goes first
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("−14 with no exiled cards still restarts the game")
        void restartWithNoExiledCards() {
            Permanent karn = addReadyKarn(player1);
            karn.setCounterCount(CounterType.LOYALTY, 14);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            // Game enters mulligan phase
            assertThat(gd.status).isEqualTo(GameStatus.MULLIGAN);

            // Complete mulligans
            harness.skipMulligan();

            // Game should have restarted — life reset, hands drawn
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        }

        @Test
        @DisplayName("Cannot activate −14 with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent karn = addReadyKarn(player1);
            assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty");
        }

        @Test
        @CardUsed({Batterskull.class})
        @DisplayName("Equipment returned by the restart triggers living weapon")
        void restartTriggersLivingWeapon() {
            Permanent karn = addReadyKarn(player1);
            Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Batterskull());
            harness.activateAbility(player1, 0, 1, null, equipment.getId());
            harness.passBothPriorities();

            karn.setLoyaltyActivationsThisTurn(0);
            karn.setCounterCount(CounterType.LOYALTY, 14);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.skipMulligan();

            assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof Batterskull);
            harness.passBothPriorities();
            Permanent germ = findPermanent(player1, "Phyrexian Germ");
            assertThat(findPermanent(player1, "Batterskull").getAttachedTo()).isEqualTo(germ.getId());
        }

        @Test
        @DisplayName("A player unable to draw seven opening cards loses after the restart")
        void restartWithTooFewOpeningCardsLoses() {
            Permanent karn = addReadyKarn(player1);
            karn.setCounterCount(CounterType.LOYALTY, 14);
            harness.setHand(player2, List.of());
            harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(),
                    new Forest(), new Forest(), new Forest()));
            gd.playerGraveyards.get(player2.getId()).clear();

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.skipMulligan();

            if (gd.status != GameStatus.FINISHED) {
                harness.forceStep(TurnStep.UPKEEP);
                harness.runStateBasedActions();
            }
            assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
            assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
        }

        @Test
        @CardUsed({PorcelainLegionnaire.class})
        @DisplayName("A second restart returns an opponent's previously acquired card to its owner")
        void secondRestartPreservesOwnership() {
            Permanent karn = addReadyKarn(player1);
            PorcelainLegionnaire card = new PorcelainLegionnaire();
            card.setOwnerId(player2.getId());
            Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
            harness.activateAbility(player1, 0, 1, null, creature.getId());
            harness.passBothPriorities();

            karn.setLoyaltyActivationsThisTurn(0);
            karn.setCounterCount(CounterType.LOYALTY, 14);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.skipMulligan();
            harness.assertOnBattlefield(player1, "Porcelain Legionnaire");

            Permanent secondKarn = addReadyKarn(player1);
            secondKarn.setCounterCount(CounterType.LOYALTY, 14);
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(secondKarn);
            harness.activateAbility(player1, index, 2, null, null);
            harness.passBothPriorities();

            List<com.github.laxika.magicalvibes.model.Card> ownerCards = new ArrayList<>(gd.playerDecks.get(player2.getId()));
            ownerCards.addAll(gd.playerHands.get(player2.getId()));
            assertThat(ownerCards).contains(card);
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(card);
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        }

        @Test
        @CardUsed({NumbingDose.class, MutagenicGrowth.class})
        @DisplayName("Auras and instants exiled from hand return to the library on restart")
        void restartDoesNotKeepAurasOrInstants() {
            Permanent karn = addReadyKarn(player1);
            NumbingDose aura = new NumbingDose();
            MutagenicGrowth instant = new MutagenicGrowth();
            harness.setHand(player2, List.of(aura, instant));
            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player2, 0);
            karn.setLoyaltyActivationsThisTurn(0);
            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player2, 0);

            karn.setLoyaltyActivationsThisTurn(0);
            karn.setCounterCount(CounterType.LOYALTY, 14);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            List<com.github.laxika.magicalvibes.model.Card> ownerCards = new ArrayList<>(gd.playerDecks.get(player2.getId()));
            ownerCards.addAll(gd.playerHands.get(player2.getId()));
            assertThat(ownerCards).contains(aura, instant);
            assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(aura, instant);
            harness.skipMulligan();
            harness.assertNotOnBattlefield(player1, "Numbing Dose");
        }

        @Test
        @DisplayName("A land exiled from hand enters before the restarted game's first turn")
        void restartKeepsLandExiledFromHand() {
            Permanent karn = addReadyKarn(player1);
            Forest forest = new Forest();
            harness.setHand(player2, List.of(forest));
            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();
            harness.handleCardChosen(player2, 0);

            karn.setLoyaltyActivationsThisTurn(0);
            karn.setCounterCount(CounterType.LOYALTY, 14);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            assertThat(gd.getPlayerExiledCards(player2.getId())).contains(forest);
            assertThat(gd.playerHands.get(player2.getId())).doesNotContain(forest);
            assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(forest);
            harness.skipMulligan();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(permanent -> permanent.getCard() == forest);
            assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(forest);
            assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isZero();
        }
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyKarn(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same Karn in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyKarn(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    @DisplayName("Karn dies when loyalty reaches 0")
    void diesWhenLoyaltyReachesZero() {
        Permanent karn = addReadyKarn(player1);
        karn.setCounterCount(CounterType.LOYALTY, 3);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        // -3 ability: 3 - 3 = 0, Karn dies to state-based actions
        harness.activateAbility(player1, 0, 1, null, bears.getId());

        harness.assertNotOnBattlefield(player1, "Karn Liberated");
        harness.assertInGraveyard(player1, "Karn Liberated");
        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("−3 ability still resolves after Karn dies to SBA at 0 loyalty")
    void abilityResolvesAfterDeath() {
        Permanent karn = addReadyKarn(player1);
        karn.setCounterCount(CounterType.LOYALTY, 3);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        // Bears should still be exiled even though Karn died
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private Permanent addReadyKarn(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KarnLiberated());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
