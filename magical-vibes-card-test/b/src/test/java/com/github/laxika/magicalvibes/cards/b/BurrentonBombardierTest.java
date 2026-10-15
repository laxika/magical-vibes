package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurrentonBombardier.class, Mutavault.class})
class BurrentonBombardierTest extends BaseCardTest {

    @Test
    @DisplayName("Reinforce puts two +1/+1 counters on target creature")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateHandAbility(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(targetCreature.getEffectivePower()).isEqualTo(4);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reinforce can target a creature controlled by an opponent")
    void reinforceTargetsOpponentCreature() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateHandAbility(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(targetCreature.getEffectivePower()).isEqualTo(4);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reinforce discards the source card to the graveyard as a cost")
    void reinforceDiscardsSourceCard() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateHandAbility(player1, 0, targetCreature.getId());

        harness.assertNotInHand(player1, "Burrenton Bombardier");
        harness.assertInGraveyard(player1, "Burrenton Bombardier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reinforce cannot be activated without enough mana; the card stays in hand")
    void reinforceRequiresMana() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Burrenton Bombardier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce cannot be activated without the required white mana")
    void reinforceRequiresWhiteMana() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Burrenton Bombardier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature permanent; no cost is paid")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Burrenton Bombardier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce requires a target before any costs are paid")
    void reinforceRequiresTarget() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Burrenton Bombardier");
        harness.assertNotInGraveyard(player1, "Burrenton Bombardier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce accepts generic mana and places counters only on resolution")
    void reinforceResolvesAfterCostsWithMixedMana() {
        harness.setHand(player1, List.of(new BurrentonBombardier()));
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, targetCreature.getId());

        harness.assertNotInHand(player1, "Burrenton Bombardier");
        harness.assertInGraveyard(player1, "Burrenton Bombardier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
