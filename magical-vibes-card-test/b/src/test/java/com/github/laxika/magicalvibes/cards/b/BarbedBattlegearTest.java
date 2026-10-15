package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbedBattlegear.class, CarapaceForger.class, Memnite.class})
class BarbedBattlegearTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires two mana")
    void equipRequiresTwoMana() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gear.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Barbed Battlegear for {3} and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BarbedBattlegear()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Barbed Battlegear")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Battlegear to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +4/-1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);    // 2 + 4
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1); // 2 - 1
    }

    @Test
    @DisplayName("Equipped creature loses boost when Battlegear is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(gear);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Battlegear does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent otherCreature = addReadyCreature(player1);
        Permanent gear = addGearReady(player1);
        gear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Battlegear can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        gear.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(6);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(gear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Carapace Forger"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gear.getAttachedTo()).isNull();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Equipping a one-toughness creature puts it into the graveyard and leaves Battlegear unattached")
    void equippedCreatureWithZeroToughnessDies() {
        Permanent gear = addGearReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertOnBattlefield(player1, "Barbed Battlegear");
        assertThat(gear.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gear.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature")
    void cannotEquipNoncreature() {
        Permanent gear = addGearReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gear.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip can only be activated as a sorcery")
    void cannotEquipDuringCombat() {
        Permanent gear = addGearReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gear.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGearReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new BarbedBattlegear());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CarapaceForger());
    }
}
