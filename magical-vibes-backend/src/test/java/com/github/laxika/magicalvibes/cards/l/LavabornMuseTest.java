package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LavabornMuseTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Lavaborn Muse has correct card properties")
    void hasCorrectProperties() {
        LavabornMuse card = new LavabornMuse();

        assertThat(card.getName()).isEqualTo("Lavaborn Muse");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.OPPONENT_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.OPPONENT_UPKEEP_TRIGGERED).getFirst()).isInstanceOf(DealDamageIfFewCardsInHandEffect.class);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Deals 3 damage when opponent has 2 cards in hand")
    void dealsDamageWithTwoCards() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals 3 damage when opponent has 1 card in hand")
    void dealsDamageWithOneCard() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals 3 damage when opponent has empty hand")
    void dealsDamageWithEmptyHand() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does NOT trigger when opponent has 3 or more cards in hand")
    void doesNotTriggerWithThreeCards() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT trigger during controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player1, List.of()); // empty hand, condition would be met
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT deal damage if opponent's hand grows above 2 before resolution")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears())); // 1 card, triggers
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Trigger is on the stack — add cards to opponent's hand before resolution
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        // Now opponent has 3 cards in hand
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}

