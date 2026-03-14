package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeteranSwordsmithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Veteran Swordsmith has static boost effect for own Soldiers")
    void hasCorrectProperties() {
        VeteranSwordsmith card = new VeteranSwordsmith();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
        assertThat(effect.grantedKeywords()).isEmpty();
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isNotNull();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Veteran Swordsmith puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VeteranSwordsmith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Veteran Swordsmith");
    }

    @Test
    @DisplayName("Resolving puts Veteran Swordsmith onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VeteranSwordsmith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Veteran Swordsmith"));
    }

    // ===== Static effect: buffs other own Soldiers =====

    @Test
    @DisplayName("Other own Soldier creatures get +1/+0")
    void buffsOtherOwnSoldiers() {
        // Aven Cloudchaser is a Bird Soldier (2/2)
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.addToBattlefield(player1, new VeteranSwordsmith());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Veteran Swordsmith does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());

        Permanent swordsmith = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Veteran Swordsmith"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, swordsmith)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swordsmith)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new VeteranSwordsmith());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Soldier creatures")
    void doesNotBuffOpponentSoldiers() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player2, new AvenCloudchaser());

        Permanent opponentSoldier = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(2);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Veteran Swordsmiths buff each other")
    void twoSwordsmithsBuffEachOther() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player1, new VeteranSwordsmith());

        List<Permanent> swordsmiths = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Veteran Swordsmith"))
                .toList();

        assertThat(swordsmiths).hasSize(2);
        for (Permanent swordsmith : swordsmiths) {
            // Each gets +1/+0 from the other → 4/2
            assertThat(gqs.getEffectivePower(gd, swordsmith)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, swordsmith)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Two Veteran Swordsmiths give +2/+0 to other Soldiers")
    void twoSwordsmithsStackBonuses() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // 2/2 base + 2/0 from two sources = 4/2
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Veteran Swordsmith leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Veteran Swordsmith"));

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Veteran Swordsmith resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.setHand(player1, List.of(new VeteranSwordsmith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        soldier.setPowerModifier(soldier.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(8); // 2 base + 5 spell + 1 static

        soldier.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(2);
    }
}
