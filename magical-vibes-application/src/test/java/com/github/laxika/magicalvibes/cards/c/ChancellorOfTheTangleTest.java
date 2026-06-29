package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class ChancellorOfTheTangleTest {

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
    @DisplayName("Chancellor of the Tangle has ON_OPENING_HAND_REVEAL MayEffect wrapping RegisterDelayedManaTriggerEffect")
    void hasOpeningHandTriggeredEffect() {
        ChancellorOfTheTangle card = new ChancellorOfTheTangle();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(RegisterDelayedManaTriggerEffect.class);
        RegisterDelayedManaTriggerEffect effect = (RegisterDelayedManaTriggerEffect) may.wrapped();
        assertThat(effect.color()).isEqualTo(ManaColor.GREEN);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Opening hand trigger =====

    @Test
    @DisplayName("Chancellor in opening hand prompts may ability at the first upkeep")
    void openingHandTriggerPromptsMayAbility() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting Chancellor reveal puts a triggered ability on the stack at the first precombat main phase")
    void acceptingRevealFiresTriggeredAbilityAtPrecombatMain() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept reveal — auto-pass advances through UPKEEP → DRAW → PRECOMBAT_MAIN,
        // where the delayed trigger fires and puts the ability on the stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chancellor of the Tangle");
        assertThat(gd.stack.getFirst().getEffectsToResolve().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals Chancellor of the Tangle"));
    }

    @Test
    @DisplayName("Declining Chancellor reveal does not put anything on the stack")
    void decliningRevealDoesNotTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.openingHandManaTriggers).isEmpty();
    }

    @Test
    @DisplayName("Resolving the trigger adds {G} to the controller's mana pool")
    void resolvingTriggerAddsGreenMana() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept reveal — registers delayed trigger, auto-advances to PRECOMBAT_MAIN
        harness.handleMayAbilityChosen(player1, true);

        // Trigger is on the stack at PRECOMBAT_MAIN — resolve it
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Chancellors in hand each trigger separately and add separate mana")
    void multipleChancellorsInHandTriggerSeparately() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle(), new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: Both MayEffects go on the stack, resolve each one
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Both delayed triggers fire at PRECOMBAT_MAIN
        assertThat(gd.stack).hasSize(2);

        // Resolve both triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chancellor stays in hand after opening hand trigger")
    void chancellorRemainsInHandAfterTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept reveal — registers delayed trigger
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the AwardMana trigger at PRECOMBAT_MAIN
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chancellor of the Tangle"));
    }

    @Test
    @DisplayName("Chancellor does not trigger from hand on subsequent turns")
    void doesNotTriggerOnSubsequentTurns() {
        harness.skipMulligan();
        // Set hand with Chancellor after the mulligan (so it wasn't in opening hand during first upkeep)
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.openingHandManaTriggers).isEmpty();
    }

    @Test
    @DisplayName("Both players can trigger Chancellors — each fires on their own first main phase")
    void bothPlayersCanTriggerChancellors() {
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.setHand(player2, List.of(new ChancellorOfTheTangle()));
        harness.skipMulligan();

        // CR 603.5: Both MayEffects go on the stack (player2's on top, resolved first)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Player 1's trigger fired at their first PRECOMBAT_MAIN (active player is player1 on turn 1)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        // Player 2's trigger should still be pending for their first main phase
        assertThat(gd.openingHandManaTriggers).hasSize(1);
        assertThat(gd.openingHandManaTriggers.getFirst().revealingPlayerId()).isEqualTo(player2.getId());
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Chancellor of the Tangle puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new ChancellorOfTheTangle()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Chancellor of the Tangle"));
    }
}
