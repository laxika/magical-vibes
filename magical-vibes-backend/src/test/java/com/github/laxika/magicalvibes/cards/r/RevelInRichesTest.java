package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RevelInRichesTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_OPPONENT_CREATURE_DIES CreateTokenEffect and UPKEEP_TRIGGERED win condition")
    void hasCorrectStructure() {
        RevelInRiches card = new RevelInRiches();

        // Death trigger: create Treasure token
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DIES).getFirst();
        assertThat(tokenEffect.primaryType()).isEqualTo(CardType.ARTIFACT);
        assertThat(tokenEffect.amount()).isEqualTo(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Treasure");

        // Upkeep trigger: win if 10+ Treasures
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ControlsPermanentCountConditionalEffect.class);
        ControlsPermanentCountConditionalEffect winCondition =
                (ControlsPermanentCountConditionalEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(winCondition.minCount()).isEqualTo(10);
        assertThat(winCondition.wrapped()).isInstanceOf(WinGameEffect.class);
    }

    // ===== Death trigger: create Treasure =====

    @Test
    @DisplayName("Creates a Treasure token when an opponent's creature dies")
    void createsTreasureWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new RevelInRiches());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger

        harness.passBothPriorities(); // Resolve death trigger (CreateTokenEffect)

        // Treasure token on player1's battlefield
        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().getCard().isToken()).isTrue();
        assertThat(treasures.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasures.getFirst().getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Does not create Treasure when controller's own creature dies")
    void noTreasureWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new RevelInRiches());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Player2 kills player1's creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → player1's bears die

        // No Treasure token should be created — trigger only fires for opponent's creatures
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Treasure"));
    }

    @Test
    @DisplayName("Creates multiple Treasures when multiple opponent creatures die")
    void createsTreasureForEachOpponentCreatureDeath() {
        harness.addToBattlefield(player1, new RevelInRiches());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill both opponent's creatures — Shock one, then the other
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bears1Id = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();
        harness.castInstant(player1, 0, bears1Id);
        harness.passBothPriorities(); // Resolve first Shock → first bears die → death trigger
        harness.passBothPriorities(); // Resolve death trigger

        UUID bears2Id = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();
        harness.castInstant(player1, 0, bears2Id);
        harness.passBothPriorities(); // Resolve second Shock → second bears die → death trigger
        harness.passBothPriorities(); // Resolve death trigger

        // Two Treasure tokens on player1's battlefield
        long treasureCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count();
        assertThat(treasureCount).isEqualTo(2);
    }

    // ===== Upkeep trigger: win condition =====

    @Test
    @DisplayName("Wins the game at upkeep with exactly 10 Treasures")
    void winsWithExactlyTenTreasures() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 10);

        advanceToUpkeep(player1);

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Revel in Riches");

        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains("wins the game"));
    }

    @Test
    @DisplayName("Wins the game at upkeep with more than 10 Treasures")
    void winsWithMoreThanTenTreasures() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 15);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // Resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with fewer than 10 Treasures")
    void doesNotTriggerWithNineTreasures() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 9);

        advanceToUpkeep(player1);

        // Trigger should NOT be on the stack (intervening-if fails)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with zero Treasures")
    void doesNotTriggerWithNoTreasures() {
        harness.addToBattlefield(player1, new RevelInRiches());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 10);

        // Advance to player2's upkeep — Revel in Riches is on player1's battlefield
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Condition re-checked on resolution — does nothing if Treasures removed")
    void interveningIfCheckedOnResolution() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 10);

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Remove Treasures before resolution (simulating interaction)
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Treasure"));

        harness.passBothPriorities(); // Resolve trigger

        // Condition no longer met — game should NOT be finished
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Death trigger + win condition interaction =====

    @Test
    @DisplayName("Death trigger Treasure creation can eventually lead to win condition")
    void deathTriggerTreasuresContributeToWinCondition() {
        harness.addToBattlefield(player1, new RevelInRiches());
        addTreasureTokens(player1, 9); // 9 Treasures already
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill opponent's creature to get 10th Treasure
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve death trigger → 10th Treasure created

        // Now advance to upkeep — should trigger win condition
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // Resolve win trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void addTreasureTokens(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card treasureToken = new Card();
            treasureToken.setName("Treasure");
            treasureToken.setType(CardType.ARTIFACT);
            treasureToken.setManaCost("");
            treasureToken.setToken(true);
            treasureToken.setSubtypes(List.of(CardSubtype.TREASURE));
            harness.addToBattlefield(player, treasureToken);
        }
    }
}
