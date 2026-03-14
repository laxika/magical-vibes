package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuturePriestTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ALLY_CREATURE_ENTERS_BATTLEFIELD MayEffect wrapping GainLifeEffect(1)")
    void hasCorrectAllyCreatureTrigger() {
        SuturePriest card = new SuturePriest();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD MayEffect wrapping TargetPlayerLosesLifeEffect(1)")
    void hasCorrectOpponentCreatureTrigger() {
        SuturePriest card = new SuturePriest();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(TargetPlayerLosesLifeEffect.class);
        assertThat(((TargetPlayerLosesLifeEffect) may.wrapped()).amount()).isEqualTo(1);
    }

    // ===== Ally creature enters — accept may =====

    @Test
    @DisplayName("Gains 1 life when accepting may after another ally creature enters")
    void gainsLifeWhenAllyCreatureEntersAccept() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → Suture Priest triggers, MayEffect on stack
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt for Suture Priest's controller
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        harness.assertLife(player1, 21);
    }

    // ===== Ally creature enters — decline may =====

    @Test
    @DisplayName("No life gain when declining may after ally creature enters")
    void noLifeGainWhenAllyCreatureEntersDecline() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    // ===== Does not trigger for itself entering =====

    @Test
    @DisplayName("Does not trigger when Suture Priest itself enters the battlefield")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Suture Priest enters
        harness.passBothPriorities();

        // No may prompt — "another creature" excludes itself
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Opponent creature enters — accept may =====

    @Test
    @DisplayName("Opponent loses 1 life when accepting may after opponent's creature enters")
    void opponentLosesLifeWhenOpponentCreatureEntersAccept() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell → Suture Priest triggers for opponent creature, MayEffect on stack
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // May ability prompt for Suture Priest's controller (player1)
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        harness.assertLife(player2, 19);
    }

    // ===== Opponent creature enters — decline may =====

    @Test
    @DisplayName("No life loss when declining may after opponent's creature enters")
    void noLifeLossWhenOpponentCreatureEntersDecline() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    // ===== Does not trigger for controller's own creature on opponent trigger =====

    @Test
    @DisplayName("Opponent creature trigger does not fire for controller's own creature")
    void opponentTriggerDoesNotFireForOwnCreature() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → only ally trigger, no opponent trigger
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Only the ally may ability should trigger (gain life), not the opponent one (lose life)
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // Player 1 gained 1 life from ally trigger
        harness.assertLife(player1, 21);
        // Player 2 life unchanged — opponent trigger did not fire
        harness.assertLife(player2, 20);
    }
}
