package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FalkenrathNobleTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_DEATH and ON_ANY_CREATURE_DIES effects")
    void hasCorrectStructure() {
        FalkenrathNoble card = new FalkenrathNoble();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class);
        TargetPlayerLosesLifeAndControllerGainsLifeEffect deathEffect =
                (TargetPlayerLosesLifeAndControllerGainsLifeEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(deathEffect.lifeLoss()).isEqualTo(1);
        assertThat(deathEffect.lifeGain()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class);
    }

    // ===== ON_DEATH: Falkenrath Noble itself dies =====

    @Test
    @DisplayName("When Falkenrath Noble dies, target player loses 1 life and controller gains 1 life")
    void selfDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill Falkenrath Noble with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID nobleId = harness.getPermanentId(player1, "Falkenrath Noble");
        harness.castInstant(player2, 0, nobleId);
        harness.passBothPriorities(); // Resolve Shock → Noble dies → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    // ===== ON_ANY_CREATURE_DIES: another creature dies =====

    @Test
    @DisplayName("When an ally creature dies, target player loses 1 life and controller gains 1 life")
    void allyCreatureDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill ally creature with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("When an opponent's creature dies, target player loses 1 life and controller gains 1 life")
    void opponentCreatureDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Death trigger can target the controller for life loss")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Controller loses 1 life AND gains 1 life (net 0)
        harness.assertLife(player1, 20);
        // Opponent unaffected
        harness.assertLife(player2, 20);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
