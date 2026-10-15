package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.k.KithkinHealer;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunedStalactite.class, FieldMarshal.class, GrizzlyBears.class,
        AmoeboidChangeling.class, KithkinHealer.class})
class RunedStalactiteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Runed Stalactite to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent stalactite = addCreatureReady(player1, new RunedStalactite());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(stalactite.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent stalactite = addCreatureReady(player1, new RunedStalactite());
        stalactite.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature becomes every creature type (gets Soldier lord boost)")
    void equippedCreatureIsEveryCreatureType() {
        // Grizzly Bears is a Bear, not a Soldier; Field Marshal boosts other Soldiers.
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent stalactite = addCreatureReady(player1, new RunedStalactite());
        stalactite.setAttachedTo(bears.getId());

        // 2/2 base + 1/1 (Stalactite) + 1/1 (Field Marshal, now that Bears is a Soldier)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and creature types when Stalactite is removed")
    void creatureLosesBonusesWhenEquipmentRemoved() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent stalactite = addCreatureReady(player1, new RunedStalactite());
        stalactite.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(stalactite);

        // Back to base 2/2, no longer a Soldier
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stalactite does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent stalactite = addCreatureReady(player1, new RunedStalactite());
        stalactite.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Being every creature type does not grant the changeling ability")
    void everyCreatureTypeDoesNotGrantChangeling() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RunedStalactite());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Later loss of all creature types prevents sharing a type with another creature")
    void laterTypeLossPreventsSharingCreatureTypes() {
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new RunedStalactite());
        Permanent equipped = harness.addToBattlefieldAndReturn(player1, new KithkinHealer());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, equipped.getId());
        harness.passBothPriorities();

        assertThat(gqs.shareCreatureType(gd, equipped, other)).isTrue();

        harness.activateAbility(player1, 0, 1, null, equipped.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, equipped)).isEmpty();
        assertThat(gqs.shareCreatureType(gd, equipped, other)).isFalse();
        assertThat(gqs.getEffectivePower(gd, equipped)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equipped)).isEqualTo(3);
    }

    @Test
    @DisplayName("Reequipping moves the bonus and creature types to the new creature")
    void reequippingMovesBonuses() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new RunedStalactite());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KithkinHealer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new RunedStalactite());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KithkinHealer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
