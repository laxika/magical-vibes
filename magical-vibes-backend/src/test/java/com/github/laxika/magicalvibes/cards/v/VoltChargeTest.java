package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoltChargeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has deal 3 damage and proliferate effects")
    void hasCorrectEffects() {
        VoltCharge card = new VoltCharge();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect dmg = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(dmg.damage()).isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ProliferateEffect.class);
        assertThat(card.isNeedsTarget()).isTrue();
    }

    // ===== Damage to player =====

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Damage to creature =====

    @Test
    @DisplayName("Deals 3 damage to target creature, destroying a 1/1")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Damage + proliferate =====

    @Test
    @DisplayName("Deals 3 damage to player and proliferates +1/+1 counter on own creature")
    void deals3DamageAndProliferates() {
        harness.setLife(player2, 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Proliferate choice
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals damage and skips proliferate when no permanents have counters")
    void dealsDamageNoProliferateNeeded() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No proliferate choice needed — no eligible permanents
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Proliferate can choose none — damage still resolves")
    void proliferateChooseNoneDamageStillResolves() {
        harness.setLife(player2, 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose no permanents to proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Cleanup =====

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new VoltCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Volt Charge"));
    }
}
