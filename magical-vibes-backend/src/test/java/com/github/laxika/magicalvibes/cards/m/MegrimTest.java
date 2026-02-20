package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticSpecter;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MegrimTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Megrim has correct card properties")
    void hasCorrectProperties() {
        Megrim card = new Megrim();

        assertThat(card.getName()).isEqualTo("Megrim");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst())
                .isInstanceOf(DealDamageToDiscardingPlayerEffect.class);
        DealDamageToDiscardingPlayerEffect effect =
                (DealDamageToDiscardingPlayerEffect) card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Megrim puts it on the stack as enchantment spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Megrim()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Megrim");
    }

    @Test
    @DisplayName("Megrim resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new Megrim()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Megrim"));
    }

    // ===== Triggered ability: opponent discards via Distress (revealed hand path) =====

    @Test
    @DisplayName("Megrim deals 2 damage when opponent discards via Distress")
    void triggersOnOpponentDiscardViaDistress() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses card from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Megrim trigger should have resolved, dealing 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Triggered ability: opponent discards via handleDiscardCardChosen path =====

    @Test
    @DisplayName("Megrim deals 2 damage when opponent discards via discard effect")
    void triggersOnOpponentDiscardViaDiscardEffect() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setLife(player2, 20);

        // Player2 casts Sift (draw 3, discard 1) — discard goes through handleDiscardCardChosen
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Give player2 a deck to draw from
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        // Player2 chooses a card to discard
        harness.handleCardChosen(player2, 0);

        // Megrim trigger should have resolved, dealing 2 damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== No trigger when controller discards =====

    @Test
    @DisplayName("Megrim does NOT trigger when its controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setLife(player1, 20);

        // Player1 casts Sift (draw 3, discard 1) — player1 is the one discarding
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        harness.handleCardChosen(player1, 0);

        // Player1's life should be unchanged — Megrim doesn't trigger on own discard
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Multiple discards trigger multiple times =====

    @Test
    @DisplayName("Megrim triggers once per card discarded via Distress (single card)")
    void triggersOncePerDiscardViaDistress() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setLife(player2, 20);

        // Distress forces discard of 1 nonland card
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Only 1 card discarded, so only 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Two Megrims each trigger =====

    @Test
    @DisplayName("Two Megrims each trigger when opponent discards, dealing 4 damage total")
    void twoMegrimsEachTrigger() {
        harness.addToBattlefield(player1, new Megrim());
        harness.addToBattlefield(player1, new Megrim());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Both Megrims trigger, dealing 2 + 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Random discard from combat triggers Megrim =====

    @Test
    @DisplayName("Megrim triggers when Hypnotic Specter forces random discard")
    void triggersOnRandomDiscardFromCombat() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        Permanent specter = new Permanent(new HypnoticSpecter());
        specter.setSummoningSick(false);
        specter.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(specter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player2 took 2 combat damage from Specter + random discard triggered Megrim for 2 more
        // Total: 20 - 2 (combat) - 2 (Megrim) = 16
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== No trigger when Megrim is not on the battlefield =====

    @Test
    @DisplayName("Megrim does not trigger when it is not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Megrim is in hand, NOT on the battlefield
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // No Megrim on battlefield, so no extra damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Megrim trigger is logged")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new Megrim());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Megrim") && log.contains("triggers") && log.contains("2 damage"));
    }

    // ===== Opponent controls Megrim — their discard does not trigger it =====

    @Test
    @DisplayName("Player2's Megrim triggers when player1 discards")
    void opponentsMegrimTriggersOnOurDiscard() {
        harness.addToBattlefield(player2, new Megrim());
        harness.setLife(player1, 20);

        // Player1 casts Sift — player1 discards, which is an opponent of player2's Megrim
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        harness.handleCardChosen(player1, 0);

        // Player2's Megrim triggers on player1's discard, dealing 2 damage to player1
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}

