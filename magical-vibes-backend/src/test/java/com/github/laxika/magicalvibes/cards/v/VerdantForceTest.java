package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdantForceTest {

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
    @DisplayName("Verdant Force has correct card properties")
    void hasCorrectProperties() {
        VerdantForce card = new VerdantForce();

        assertThat(card.getName()).isEqualTo("Verdant Force");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{5}{G}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(card.getPower()).isEqualTo(7);
        assertThat(card.getToughness()).isEqualTo(7);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);
    }

    // ===== Triggering during controller's upkeep =====

    @Test
    @DisplayName("Creates a 1/1 green Saproling token during controller's upkeep")
    void createsTokenDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new VerdantForce());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent saproling = tokens.getFirst();
        assertThat(saproling.getCard().getName()).isEqualTo("Saproling");
        assertThat(saproling.getCard().getPower()).isEqualTo(1);
        assertThat(saproling.getCard().getToughness()).isEqualTo(1);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    // ===== Triggering during opponent's upkeep =====

    @Test
    @DisplayName("Creates a Saproling token during opponent's upkeep under controller's control")
    void createsTokenDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new VerdantForce());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        // Token should be on player1's battlefield (the controller), not player2's
        List<Permanent> p1Tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        List<Permanent> p2Tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(p1Tokens).hasSize(1);
        assertThat(p1Tokens.getFirst().getCard().getName()).isEqualTo("Saproling");
        assertThat(p2Tokens).isEmpty();
    }

    // ===== Multiple upkeeps =====

    @Test
    @DisplayName("Creates a token on each upkeep, accumulating over multiple turns")
    void createsTokensOverMultipleUpkeeps() {
        harness.addToBattlefield(player1, new VerdantForce());

        // Player1's upkeep
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // Player2's upkeep
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
    }

    // ===== Multiple Verdant Forces =====

    @Test
    @DisplayName("Two Verdant Forces each create a token during upkeep")
    void twoVerdantForcesEachCreateToken() {
        harness.addToBattlefield(player1, new VerdantForce());
        harness.addToBattlefield(player1, new VerdantForce());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
    }
}
