package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RampagingFerocidonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has PlayersCantGainLifeEffect as a STATIC effect")
    void hasPlayersCantGainLifeStaticEffect() {
        RampagingFerocidon card = new RampagingFerocidon();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PlayersCantGainLifeEffect.class);
    }

    @Test
    @DisplayName("Has DealDamageToTargetPlayerEffect(1) on ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD")
    void hasCreatureEntersTrigger() {
        RampagingFerocidon card = new RampagingFerocidon();

        assertThat(card.getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(DealDamageToTargetPlayerEffect.class);
        DealDamageToTargetPlayerEffect effect =
                (DealDamageToTargetPlayerEffect) card.getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    // ===== Players can't gain life =====

    @Test
    @DisplayName("Opponent can't gain life while Ferocidon is on the battlefield")
    void opponentCantGainLife() {
        harness.addToBattlefield(player1, new RampagingFerocidon());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast Angel of Mercy (ETB: gain 3 life) for player2
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Ferocidon damage trigger
        harness.passBothPriorities(); // resolve ETB gain life effect

        // Life gain was prevented; only the 1 damage from Ferocidon applies
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Controller also can't gain life while Ferocidon is on the battlefield")
    void controllerCantGainLife() {
        harness.addToBattlefield(player1, new RampagingFerocidon());

        // Cast Angel of Mercy (ETB: gain 3 life) for player1
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Ferocidon damage trigger
        harness.passBothPriorities(); // resolve ETB gain life effect

        // Life gain was prevented; only the 1 damage from Ferocidon applies
        harness.assertLife(player1, 19);
    }

    // ===== Creature enters — deals 1 damage to that creature's controller =====

    @Test
    @DisplayName("Deals 1 damage to opponent when opponent's creature enters")
    void deals1DamageWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new RampagingFerocidon());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities(); // resolve creature spell → Ferocidon triggers
        harness.passBothPriorities(); // resolve Ferocidon damage trigger

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to controller when controller's own creature enters")
    void deals1DamageWhenControllerCreatureEnters() {
        harness.addToBattlefield(player1, new RampagingFerocidon());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Ferocidon triggers
        harness.passBothPriorities(); // resolve Ferocidon damage trigger

        harness.assertLife(player1, 19);
    }

    // ===== Does not trigger for itself entering =====

    @Test
    @DisplayName("Does not trigger when Rampaging Ferocidon itself enters the battlefield")
    void doesNotTriggerForSelfEntering() {
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new RampagingFerocidon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell

        // No trigger — "another creature" excludes itself
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    // ===== Multiple creatures entering =====

    @Test
    @DisplayName("Triggers separately for each creature that enters")
    void triggersForEachCreature() {
        harness.addToBattlefield(player1, new RampagingFerocidon());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // First creature
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Ferocidon trigger

        // Second creature
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Ferocidon trigger

        harness.assertLife(player2, 18);
    }

    // ===== Trigger stops after Ferocidon leaves =====

    @Test
    @DisplayName("No longer triggers after Ferocidon leaves the battlefield")
    void noTriggerAfterFerocidonLeaves() {
        harness.addToBattlefield(player1, new RampagingFerocidon());
        harness.setLife(player2, 20);

        // Remove Ferocidon from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        // No trigger — Ferocidon is gone
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    // ===== Life gain works after Ferocidon leaves =====

    @Test
    @DisplayName("Life gain works again after Ferocidon leaves the battlefield")
    void lifeGainWorksAfterFerocidonLeaves() {
        harness.addToBattlefield(player1, new RampagingFerocidon());

        // Remove Ferocidon
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast Angel of Mercy (ETB: gain 3 life) for player2
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB gain life effect

        harness.assertLife(player2, 23);
    }
}
