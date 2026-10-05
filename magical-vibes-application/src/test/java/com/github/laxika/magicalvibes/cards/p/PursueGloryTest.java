package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PursueGlory.class, PouncingCheetah.class})
class PursueGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Pursue Glory boosts only attacking creatures with +2/+0")
    void boostsAttackingCreatures() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new PouncingCheetah());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new PursueGlory(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        assertThat(nonAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pursue Glory boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new PursueGlory(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling discards Pursue Glory and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PursueGlory()));
        harness.setLibrary(player1, List.of(new PouncingCheetah()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Pursue Glory");
        harness.assertInHand(player1, "Pouncing Cheetah");
    }

    @Test
    void boostsOpponentsAttackersWhenCastByDefender() {
        Permanent attacker = addCreatureReady(player2, new PouncingCheetah());
        attacker.setAttacking(true);
        Permanent defender = addCreatureReady(player1, new PouncingCheetah());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castFromHand(player1, new PursueGlory(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(defender.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent removedAttacker = addCreatureReady(player1, new PouncingCheetah());
        removedAttacker.setAttacking(true);
        Permanent newAttacker = addCreatureReady(player1, new PouncingCheetah());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player1, new PursueGlory(), "{3}{R}");

        removedAttacker.setAttacking(false);
        newAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(removedAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(newAttacker.getEffectivePower()).isEqualTo(5);

        newAttacker.setAttacking(false);
        removedAttacker.setAttacking(true);
        Permanent laterAttacker = addCreatureReady(player1, new PouncingCheetah());
        laterAttacker.setAttacking(true);

        assertThat(newAttacker.getEffectivePower()).isEqualTo(5);
        assertThat(removedAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(laterAttacker.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void cyclingPaysDiscardImmediatelyAndDoesNotBoostAttackers() {
        Permanent attacker = addCreatureReady(player1, new PouncingCheetah());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new PursueGlory()));
        harness.setLibrary(player1, List.of(new PouncingCheetah()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Pursue Glory");
        harness.assertInGraveyard(player1, "Pursue Glory");
        harness.assertNotInHand(player1, "Pouncing Cheetah");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Pouncing Cheetah");
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
    }

    @Test
    void resolvesWithoutAttackersAndDoesNotBoostLaterAttackers() {
        Permanent creature = addCreatureReady(player1, new PouncingCheetah());
        harness.castFromHand(player1, new PursueGlory(), "{3}{R}");
        harness.passBothPriorities();

        creature.setAttacking(true);

        harness.assertInGraveyard(player1, "Pursue Glory");
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
    }
}
