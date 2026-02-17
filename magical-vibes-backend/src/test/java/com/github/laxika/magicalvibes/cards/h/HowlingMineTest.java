package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HowlingMineTest {

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

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Howling Mine has correct card properties")
    void hasCorrectProperties() {
        HowlingMine card = new HowlingMine();

        assertThat(card.getName()).isEqualTo("Howling Mine");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{2}");
        assertThat(card.getEffects(EffectSlot.EACH_DRAW_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_DRAW_TRIGGERED).getFirst())
                .isInstanceOf(DrawCardForTargetPlayerEffect.class);
        DrawCardForTargetPlayerEffect effect =
                (DrawCardForTargetPlayerEffect) card.getEffects(EffectSlot.EACH_DRAW_TRIGGERED).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.requireSourceUntapped()).isTrue();
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Active player draws an additional card during their draw step")
    void triggersDrawForActivePlayer() {
        harness.addToBattlefield(player1, new HowlingMine());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Howling Mine trigger

        // Normal draw + Howling Mine draw = 2 total
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Opponent draws an additional card during their draw step")
    void triggersDrawForOpponent() {
        harness.addToBattlefield(player1, new HowlingMine());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Howling Mine trigger

        // Normal draw + Howling Mine draw = 2 total
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Only the active player draws, not the controller")
    void onlyActivePlayerDrawsExtra() {
        harness.addToBattlefield(player1, new HowlingMine());
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Howling Mine trigger

        // Player1 (controller) should NOT draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Player2 (active player) draws normal + 1 extra
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 2);
    }

    // ===== Untapped condition (intervening-if) =====

    @Test
    @DisplayName("Does not trigger when tapped")
    void doesNotTriggerWhenTapped() {
        harness.addToBattlefield(player1, new HowlingMine());
        Permanent howlingMine = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Howling Mine"))
                .findFirst().orElseThrow();
        howlingMine.tap();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);

        // Only the normal draw — no trigger was created
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Tapping after trigger but before resolution prevents the extra draw (intervening-if)")
    void interveningIfPreventsDrawWhenTappedBeforeResolution() {
        harness.addToBattlefield(player1, new HowlingMine());
        Permanent howlingMine = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Howling Mine"))
                .findFirst().orElseThrow();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        // Trigger is now on the stack — tap the Howling Mine before resolution
        howlingMine.tap();
        harness.passBothPriorities(); // resolve trigger — should fizzle

        // Only the normal draw, Howling Mine trigger fizzled
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Removing source from battlefield after trigger still allows the draw (last known state was untapped)")
    void triggerStillResolvesWhenSourceLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new HowlingMine());
        Permanent howlingMine = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Howling Mine"))
                .findFirst().orElseThrow();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        // Trigger is on the stack — destroy Howling Mine before resolution
        gd.playerBattlefields.get(player1.getId()).remove(howlingMine);
        harness.passBothPriorities(); // resolve trigger — should still draw (last known state was untapped)

        // Normal draw + Howling Mine draw = 2 total
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    // ===== Turn 1 skip =====

    @Test
    @DisplayName("Does not trigger on first turn for the starting player (draw step is skipped)")
    void doesNotTriggerOnFirstTurn() {
        harness.addToBattlefield(player1, new HowlingMine());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        gd.turnNumber = 1;
        gd.startingPlayerId = player1.getId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW — but entire step is skipped

        // No draws at all — entire draw step skipped per rule 103.7a
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    // ===== Multiple Howling Mines =====

    @Test
    @DisplayName("Two Howling Mines cause two additional draws")
    void multipleHowlingMinesStack() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new HowlingMine());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        // Normal draw + 2 Howling Mine draws = 3 total
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }
}
