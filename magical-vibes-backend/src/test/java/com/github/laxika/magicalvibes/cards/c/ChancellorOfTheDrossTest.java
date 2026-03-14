package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChancellorOfTheDrossTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — opening hand tests need to set hand first
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Chancellor of the Dross has ON_OPENING_HAND_REVEAL MayEffect wrapping the drain effect")
    void hasOpeningHandTriggeredEffect() {
        ChancellorOfTheDross card = new ChancellorOfTheDross();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(EachOpponentLosesLifeAndControllerGainsLifeLostEffect.class);
        EachOpponentLosesLifeAndControllerGainsLifeLostEffect effect =
                (EachOpponentLosesLifeAndControllerGainsLifeLostEffect) may.wrapped();
        assertThat(effect.amount()).isEqualTo(3);
    }

    // ===== Opening hand trigger =====

    @Test
    @DisplayName("Chancellor in opening hand prompts may ability at the first upkeep")
    void openingHandTriggerPromptsMayAbility() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Game should be awaiting may ability choice from player1
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("CR 603.5: MayEffect triggered ability goes on the stack at first upkeep")
    void mayEffectGoesOnStackAtFirstUpkeep() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack immediately (not as a pending may ability)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chancellor of the Dross");
    }

    @Test
    @DisplayName("Resolving Chancellor trigger causes opponent to lose 3 life and controller to gain 3 life")
    void openingHandTriggerDrainsLife() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept the may ability — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Opponent loses 3 life: 20 - 3 = 17
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Controller gains 3 life: 20 + 3 = 23
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Declining Chancellor reveal does not put anything on the stack")
    void decliningRevealDoesNotTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No life change
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple Chancellors in hand each prompt separately")
    void multipleChancellorsInHandTriggerSeparately() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross(), new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();

        // CR 603.5: Both MayEffects go on the stack, resolve each one
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Opponent loses 6 life total: 20 - 6 = 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        // Controller gains 6 life total: 20 + 6 = 26
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Both players can trigger Chancellors from their opening hands")
    void bothPlayersCanTriggerChancellors() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.setHand(player2, List.of(new ChancellorOfTheDross()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.skipMulligan();

        // CR 603.5: Both MayEffects go on the stack (player2's on top, resolved first)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Each player loses 3 from opponent's trigger and gains 3 from their own trigger
        // Net effect: each player is at 20 - 3 + 3 = 20
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Chancellor does not trigger from hand on subsequent turns")
    void doesNotTriggerOnSubsequentTurns() {
        harness.skipMulligan();
        // Set hand with Chancellor after the mulligan (so it wasn't in opening hand during first upkeep)
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));

        // No trigger should have happened
        assertThat(gd.stack).isEmpty();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Chancellor of the Dross puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Chancellor of the Dross"));
    }

    @Test
    @DisplayName("Chancellor stays in hand after opening hand trigger (it is not removed)")
    void chancellorRemainsInHandAfterTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheDross()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Chancellor should still be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chancellor of the Dross"));
    }
}
