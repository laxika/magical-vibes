package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VigilForTheLostTest extends BaseCardTest {

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Vigil for the Lost has ON_ALLY_CREATURE_DIES effect with PayXManaGainXLifeEffect")
    void hasCorrectEffects() {
        VigilForTheLost card = new VigilForTheLost();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst())
                .isInstanceOf(PayXManaGainXLifeEffect.class);
    }

    // ===== Triggering =====

    @Test
    @DisplayName("When controller's creature dies, Vigil for the Lost's triggered ability goes on the stack")
    void triggersWhenControllerCreatureDies() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers on the stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Triggered ability should be on the stack (not a may ability prompt)
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Vigil for the Lost");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(PayXManaGainXLifeEffect.class);
    }

    // ===== X value choice interaction =====

    @Test
    @DisplayName("Resolving the trigger with mana prompts for X value choice")
    void resolvingPromptsForXValueChoice() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        // Give player1 mana to pay for X
        harness.addMana(player1, ManaColor.WHITE, 5);

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → should prompt for X value choice
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.X_VALUE_CHOICE)).isTrue();
        InteractionContext.XValueChoice ctx = gd.interaction.xValueChoiceContext();
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player1.getId());
        assertThat(ctx.maxValue()).isEqualTo(5);
        assertThat(ctx.cardName()).isEqualTo("Vigil for the Lost");
    }

    @Test
    @DisplayName("Choosing X value pays mana and gains that much life")
    void choosingXValuePaysManaAndGainsLife() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 5);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → prompts for X value
        harness.passBothPriorities();

        // Choose X=3
        harness.handleXValueChosen(player1, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        // 5 mana - 3 paid = 2 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing X=0 gains no life and pays no mana")
    void choosingZeroGainsNothing() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 5);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → prompts for X value
        harness.passBothPriorities();

        // Choose X=0
        harness.handleXValueChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Choosing max X pays all mana and gains that much life")
    void choosingMaxXPaysAllMana() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 5);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → prompts for X value
        harness.passBothPriorities();

        // Choose X=5 (max)
        harness.handleXValueChosen(player1, 5);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Resolving with no mana =====

    @Test
    @DisplayName("Resolving the trigger with no mana gains no life (no X choice prompt)")
    void resolvingWithNoManaGainsNothing() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → no mana, no X choice prompt, no life gain
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Mixed mana colors =====

    @Test
    @DisplayName("X value choice with mixed mana colors works correctly")
    void xValueChoiceWithMixedMana() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());

        // Add mixed mana
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Resolve Cruel Edict → creature dies → Vigil triggers
        harness.passBothPriorities();

        // Resolve Vigil's triggered ability → prompts for X value (max 4)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.X_VALUE_CHOICE)).isTrue();
        assertThat(gd.interaction.xValueChoiceContext().maxValue()).isEqualTo(4);

        // Choose X=4 (all mana)
        harness.handleXValueChosen(player1, 4);

        gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Does not trigger for opponent's creatures =====

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 casts Cruel Edict targeting player2's creature
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.passBothPriorities(); // Resolve Cruel Edict

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // No trigger on the stack
        assertThat(gd.stack).isEmpty();
    }

    // ===== Multiple triggers =====

    @Test
    @DisplayName("Multiple creatures dying triggers Vigil for each one")
    void multipleCreaturesDyingTriggersForEach() {
        harness.addToBattlefield(player1, new VigilForTheLost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Use Wrath of God to kill both creatures
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.w.WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);

        // Resolve Wrath of God → both creatures die → two Vigil triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long vigilTriggers = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Vigil for the Lost"))
                .count();
        assertThat(vigilTriggers).isEqualTo(2);
    }
}
