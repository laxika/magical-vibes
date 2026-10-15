package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwellOfCourage.class, BurrentonBombardier.class, Mutavault.class})
class SwellOfCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Cast as an instant boosts all creatures you control +2/+2")
    void spellBoostsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());
        harness.castFromHand(player1, new SwellOfCourage(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(4);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spell boost wears off at cleanup")
    void spellBoostWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.castFromHand(player1, new SwellOfCourage(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reinforce X puts X +1/+1 counters on target creature")
    void reinforcePutsXCounters() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        // Reinforce 3—{3}{W}{W}
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, creature.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Swell of Courage");
    }

    @Test
    @DisplayName("Reinforce can target an opponent's creature")
    void reinforceCanTargetOpponentsCreature() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, creature.getId(), 3);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Reinforce X allows X=0 and still pays the colored cost")
    void reinforceAllowsZeroX() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, creature.getId(), 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Swell of Courage");
    }

    @Test
    @DisplayName("Reinforce cannot be activated without enough mana; the card stays in hand")
    void reinforceRequiresMana() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId(), 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Swell of Courage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce cannot be activated without the required white mana")
    void reinforceRequiresWhiteMana() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId(), 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Swell of Courage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature; no cost is paid")
    void reinforceRejectsNonCreature() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId(), 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Swell of Courage");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after the spell resolves do not receive its boost")
    void spellDoesNotBoostLaterCreatures() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.castFromHand(player1, new SwellOfCourage(), "{3}{W}{W}");
        harness.passBothPriorities();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new BurrentonBombardier());

        assertThat(original.getEffectivePower()).isEqualTo(4);
        assertThat(original.getEffectiveToughness()).isEqualTo(4);
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reinforce discards the card as a cost before placing counters on resolution")
    void reinforceDiscardsBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.setHand(player1, List.of(new SwellOfCourage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, creature.getId(), 3);

        harness.assertInGraveyard(player1, "Swell of Courage");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce counters remain after cleanup")
    void reinforceCountersRemainAfterCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.setHand(player1, List.of(new SwellOfCourage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, creature.getId(), 3);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }
}
