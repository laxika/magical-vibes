package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StriderHarness.class, CarapaceForger.class})
class StriderHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Strider Harness and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new StriderHarness()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Strider Harness")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip ability attaches Strider Harness to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent harnessPerm = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(harnessPerm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature loses boost when Strider Harness is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(harnessPerm);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Strider Harness does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent otherCreature = addCreatureReady(player1, new CarapaceForger());
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has haste")
    void equippedCreatureHasHaste() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses haste when Strider Harness is removed")
    void creatureLosesHasteWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(harnessPerm);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick creature with Strider Harness can attack")
    void summoningSickCreatureCanAttackWithHaste() {
        Permanent creature = addSummoningSickCreature(player1);
        Permanent harnessPerm = addHarnessReady(player1);
        harnessPerm.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);
        declareAttackers(List.of(0));

        resolveCombat();

        // Creature has 3 power (2 + 1 from harness), deals damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Strider Harness can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent harnessPerm = addHarnessReady(player1);
        Permanent creature1 = addCreatureReady(player1, new CarapaceForger());
        Permanent creature2 = addCreatureReady(player1, new CarapaceForger());

        harnessPerm.setAttachedTo(creature1.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(harnessPerm.getAttachedTo()).isEqualTo(creature2.getId());
        // creature1 loses all bonuses
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.HASTE)).isFalse();
        // creature2 gains all bonuses
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip requires one mana")
    void cannotEquipWithoutMana() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, newly entered Strider Harness can equip")
    void tappedEquipmentCanEquip() {
        Permanent equipment = addHarnessReady(player1);
        equipment.setSummoningSick(true);
        equipment.tap();
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void cannotEquipNoncreature() {
        Permanent equipment = addHarnessReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated while the stack is nonempty")
    void cannotEquipWithNonemptyStack() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("A failed re-equip leaves the original creature's bonuses intact")
    void targetLeavingDoesNotDetachEquipment() {
        Permanent equipment = addHarnessReady(player1);
        Permanent original = addCreatureReady(player1, new CarapaceForger());
        Permanent target = addCreatureReady(player1, new CarapaceForger());
        equipment.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipment still grants its bonuses to an attached opponent-controlled creature")
    void bonusesApplyAcrossControllers() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player2, new CarapaceForger());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Losing the equipment before attacking restores summoning sickness restrictions")
    void summoningSickCreatureCannotAttackAfterLosingHarness() {
        Permanent creature = addSummoningSickCreature(player1);
        Permanent equipment = addHarnessReady(player1);
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(equipment);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during the opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        Permanent equipment = addHarnessReady(player1);
        Permanent creature = addCreatureReady(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(equipment.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addHarnessReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new StriderHarness());
    }

    private Permanent addSummoningSickCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CarapaceForger());
        perm.setSummoningSick(true);
        return perm;
    }
}
