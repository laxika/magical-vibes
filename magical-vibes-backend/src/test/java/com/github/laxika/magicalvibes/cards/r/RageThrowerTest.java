package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RageThrowerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ANY_CREATURE_DIES effect with DealDamageToTargetPlayerEffect(2)")
    void hasCorrectStructure() {
        RageThrower card = new RageThrower();

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(DealDamageToTargetPlayerEffect.class);
        DealDamageToTargetPlayerEffect effect =
                (DealDamageToTargetPlayerEffect) card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    // ===== Triggered ability: another creature dies =====

    @Test
    @DisplayName("When an ally creature dies, Rage Thrower deals 2 damage to target player")
    void allyCreatureDeathDeals2Damage() {
        harness.addToBattlefield(player1, new RageThrower());
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

        // Target player takes 2 damage
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("When an opponent's creature dies, Rage Thrower deals 2 damage to target player")
    void opponentCreatureDeathDeals2Damage() {
        harness.addToBattlefield(player1, new RageThrower());
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

        // Target player takes 2 damage
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Rage Thrower does NOT trigger when it dies itself")
    void doesNotTriggerOnOwnDeath() {
        harness.addToBattlefield(player1, new RageThrower());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill Rage Thrower with Shock (4/2 creature, 2 damage is lethal)
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID throwerId = harness.getPermanentId(player1, "Rage Thrower");
        harness.castInstant(player2, 0, throwerId);
        harness.passBothPriorities(); // Resolve Shock → Rage Thrower dies

        // No death trigger target selection should occur — life totals unchanged
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger can target the controller")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new RageThrower());
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

        // Controller takes 2 damage
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
