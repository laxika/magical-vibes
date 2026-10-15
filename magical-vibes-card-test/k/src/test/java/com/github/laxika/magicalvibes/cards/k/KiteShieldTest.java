package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KiteShield.class, RuneclawBear.class})
class KiteShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip {3} attaches Kite Shield to target creature you control")
    void resolvingEquipAttachesToCreature() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature loses the boost when Kite Shield leaves the battlefield")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kite Shield does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent otherCreature = addCreatureReady(player1, new RuneclawBear());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} costs three mana")
    void equipCostsThreeMana() {
        harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot equip with only two mana")
    void cannotEquipWithTwoMana() {
        harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void reEquipMovesBoostOnlyOnResolution() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        shield.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(shield.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReEquipLeavesOriginalAttachment() {
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new KiteShield());
        Permanent first = addCreatureReady(player1, new RuneclawBear());
        Permanent second = addCreatureReady(player1, new RuneclawBear());
        shield.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);

        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
