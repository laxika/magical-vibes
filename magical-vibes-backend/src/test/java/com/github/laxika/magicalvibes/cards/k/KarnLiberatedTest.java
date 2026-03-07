package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KarnRestartGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarnLiberatedTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three activated abilities with correct loyalty costs")
    void hasThreeAbilities() {
        KarnLiberated card = new KarnLiberated();

        assertThat(card.getActivatedAbilities()).hasSize(3);

        var plus4 = card.getActivatedAbilities().get(0);
        assertThat(plus4.getLoyaltyCost()).isEqualTo(4);
        assertThat(plus4.isNeedsTarget()).isTrue();
        assertThat(plus4.getEffects().getFirst()).isInstanceOf(TargetPlayerExilesFromHandEffect.class);

        var minus3 = card.getActivatedAbilities().get(1);
        assertThat(minus3.getLoyaltyCost()).isEqualTo(-3);
        assertThat(minus3.isNeedsTarget()).isTrue();
        assertThat(minus3.getEffects().getFirst()).isInstanceOf(ExileTargetPermanentAndTrackWithSourceEffect.class);

        var minus14 = card.getActivatedAbilities().get(2);
        assertThat(minus14.getLoyaltyCost()).isEqualTo(-14);
        assertThat(minus14.isNeedsTarget()).isFalse();
        assertThat(minus14.getEffects().getFirst()).isInstanceOf(KarnRestartGameEffect.class);
    }

    // ===== Casting =====

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

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Karn Liberated"));
        Permanent karn = bf.stream().filter(p -> p.getCard().getName().equals("Karn Liberated")).findFirst().orElseThrow();
        assertThat(karn.getLoyaltyCounters()).isEqualTo(6);
        assertThat(karn.isSummoningSick()).isFalse();
    }

    // ===== +4 ability: Target player exiles a card from their hand =====

    @Nested
    @DisplayName("+4 ability")
    class PlusFourAbility {

        @Test
        @DisplayName("+4 increases loyalty and prompts target to exile from hand")
        void plusFourIncreasesLoyaltyAndPrompts() {
            Permanent karn = addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(karn.getLoyaltyCounters()).isEqualTo(10); // 6 + 4
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.EXILE_FROM_HAND_CHOICE);
            assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Target player exiles a card of their choice")
        void targetExilesCardOfChoice() {
            Permanent karn = addReadyKarn(player1);
            harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player2.getId());
            harness.passBothPriorities();

            // Target player (player2) chooses which card to exile
            harness.handleCardChosen(player2, 0); // exile Grizzly Bears

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");

            // Card should be in exile, not graveyard
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
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
            List<com.github.laxika.magicalvibes.model.Card> karnExiled = gd.permanentExiledCards.get(karn.getId());
            assertThat(karnExiled).isNotNull();
            assertThat(karnExiled).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Can target self to exile from own hand")
        void canTargetSelf() {
            Permanent karn = addReadyKarn(player1);
            harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

            harness.activateAbility(player1, 0, 0, null, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.EXILE_FROM_HAND_CHOICE);
            assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());

            harness.handleCardChosen(player1, 1); // exile Forest

            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.playerExiledCards.get(player1.getId()))
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
            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to exile"));
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

    // ===== −3 ability: Exile target permanent =====

    @Nested
    @DisplayName("−3 ability")
    class MinusThreeAbility {

        @Test
        @DisplayName("−3 exiles target permanent and decreases loyalty")
        void minusThreeExilesTargetPermanent() {
            Permanent karn = addReadyKarn(player1);
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");

            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            assertThat(karn.getLoyaltyCounters()).isEqualTo(3); // 6 - 3
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
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

            List<com.github.laxika.magicalvibes.model.Card> karnExiled = gd.permanentExiledCards.get(karn.getId());
            assertThat(karnExiled).isNotNull();
            assertThat(karnExiled).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Can exile own permanent")
        void canExileOwnPermanent() {
            Permanent karn = addReadyKarn(player1);
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent bears = findPermanent(player1, "Grizzly Bears");

            // Karn is at index 0, bears at index 1
            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot activate with insufficient loyalty")
        void cannotActivateWithInsufficientLoyalty() {
            Permanent karn = addReadyKarn(player1);
            karn.setLoyaltyCounters(2); // Less than 3
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty");
        }
    }

    // ===== −14 ability: Restart the game =====

    @Nested
    @DisplayName("−14 ability")
    class MinusFourteenAbility {

        @Test
        @DisplayName("−14 restarts the game and puts exiled cards onto battlefield")
        void restartGamePutsExiledCardsOntoBattlefield() {
            Permanent karn = addReadyKarn(player1);
            karn.setLoyaltyCounters(14);

            // Exile a creature with Karn's -3 first
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = findPermanent(player2, "Grizzly Bears");
            harness.activateAbility(player1, 0, 1, null, bears.getId());
            harness.passBothPriorities();

            // Verify bear is tracked with Karn
            assertThat(gd.permanentExiledCards.get(karn.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));

            // Reset loyalty ability flag and set loyalty to 14
            karn.setLoyaltyAbilityUsedThisTurn(false);
            karn.setLoyaltyCounters(14);

            // Activate -14 (ultimate)
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            // Game enters mulligan phase after restart (CR 726)
            assertThat(gd.status).isEqualTo(GameStatus.MULLIGAN);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(7);

            // Complete mulligans — Karn's exiled cards enter battlefield
            harness.skipMulligan();

            // Grizzly Bears should be on player1's battlefield (controller of Karn)
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Controller goes first
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("−14 with no exiled cards still restarts the game")
        void restartWithNoExiledCards() {
            Permanent karn = addReadyKarn(player1);
            karn.setLoyaltyCounters(14);

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
            assertThat(karn.getLoyaltyCounters()).isEqualTo(6);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough loyalty");
        }
    }

    // ===== Loyalty ability restrictions =====

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

    // ===== Planeswalker dies at 0 loyalty =====

    @Test
    @DisplayName("Karn dies when loyalty reaches 0")
    void diesWhenLoyaltyReachesZero() {
        Permanent karn = addReadyKarn(player1);
        karn.setLoyaltyCounters(3);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        // -3 ability: 3 - 3 = 0, Karn dies to state-based actions
        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Karn Liberated"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Karn Liberated"));
        // Ability is still on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("−3 ability still resolves after Karn dies to SBA at 0 loyalty")
    void abilityResolvesAfterDeath() {
        Permanent karn = addReadyKarn(player1);
        karn.setLoyaltyCounters(3);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        // Bears should still be exiled even though Karn died
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name)).findFirst().orElseThrow();
    }

    private Permanent addReadyKarn(Player player) {
        KarnLiberated card = new KarnLiberated();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(6);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
