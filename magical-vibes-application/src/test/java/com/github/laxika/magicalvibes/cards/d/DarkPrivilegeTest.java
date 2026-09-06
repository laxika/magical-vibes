package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralAtoll;
import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralAtoll.class, DarkPrivilege.class, LongbowArcher.class})
class DarkPrivilegeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Dark Privilege attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof DarkPrivilege
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost stops when Dark Privilege is removed")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a creature regenerates the enchanted creature")
    void sacrificingCreatureRegeneratesEnchanted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        // aura is index 2 (creature 0, fodder 1)
        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(fodder.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @CardUsed(Fireblast.class)
    @DisplayName("Regeneration shield saves the enchanted creature from lethal damage")
    void regenerationShieldPreventsLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate regenerate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        Permanent aura = new Permanent(new DarkPrivilege());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dark Privilege")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LongbowArcher());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new CoralAtoll());
        harness.setHand(player1, List.of(new DarkPrivilege()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
