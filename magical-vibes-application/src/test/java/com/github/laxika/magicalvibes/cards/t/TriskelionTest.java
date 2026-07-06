package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CounterType;

class TriskelionTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB three +1/+1 counters and one activated ability")
    void hasCorrectEffectsAndAbility() {
        Triskelion card = new Triskelion();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect etb = (PutCountersOnSourceEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etb.powerModifier()).isEqualTo(1);
        assertThat(etb.toughnessModifier()).isEqualTo(1);
        assertThat(etb.amount()).isEqualTo(3);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(RemoveCounterFromSourceCost.class);
                    assertThat(effects.get(1)).isInstanceOf(DealDamageToAnyTargetEffect.class);
                    assertThat(((DealDamageToAnyTargetEffect) effects.get(1)).damage()).isEqualTo(new Fixed(1));
                });
    }

    // ===== ETB: enters with three +1/+1 counters =====

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters (1/1 becomes 4/4)")
    void entersWithThreePlusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Triskelion()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent triskelion = findTriskelion(player1);

        assertThat(triskelion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(triskelion.getEffectivePower()).isEqualTo(4);
        assertThat(triskelion.getEffectiveToughness()).isEqualTo(4);
    }

    // ===== Activated ability: deal 1 damage to any target =====

    @Test
    @DisplayName("Activated ability deals 1 damage to target creature")
    void abilityDeals1DamageToCreature() {
        Permanent triskelion = addReadyTriskelion(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability deals 1 damage to target player")
    void abilityDeals1DamageToPlayer() {
        Permanent triskelion = addReadyTriskelion(player1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activated ability removes one +1/+1 counter as cost")
    void abilityRemovesOneCounter() {
        Permanent triskelion = addReadyTriskelion(player1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(triskelion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(triskelion.getEffectivePower()).isEqualTo(3);
        assertThat(triskelion.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability multiple times to remove multiple counters")
    void canActivateMultipleTimes() {
        Permanent triskelion = addReadyTriskelion(player1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Activate three times (use all three counters)
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(triskelion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Cannot activate without counters =====

    @Test
    @DisplayName("Cannot activate ability when zero +1/+1 counters remain")
    void cannotActivateWithZeroCounters() {
        Permanent triskelion = addReadyTriskelion(player1);
        triskelion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    // ===== Helpers =====

    private Permanent addReadyTriskelion(Player player) {
        Triskelion card = new Triskelion();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findTriskelion(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Triskelion"))
                .findFirst().orElseThrow();
    }
}
