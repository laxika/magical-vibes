package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlateArmor.class, PaladinsShield.class, GrizzlyBears.class, GiantGrowth.class, ProdigalPyromancer.class})
class PlateArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip costs one less for each other Equipment controlled")
    void equipCostIsReducedByOtherEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        harness.addToBattlefield(player1, new PaladinsShield());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip does not count Plate Armor itself")
    void equipDoesNotCountItself() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PlateArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent creature = addCreatureReady(player1);
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(creature.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Paying ward lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent creature = addCreatureReady(player1);
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(creature.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    void equipCanBeFreeWithMoreThanThreeOtherEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PlateArmor());
        }

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsEquipmentDoesNotReduceEquipCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PlateArmor());
        harness.addToBattlefield(player2, new PlateArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PlateArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    void reequippingMovesBoostAndWardToNewCreature() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(5);

        prepareOpponentTurn();
        harness.setHand(player2, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, original.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);

        harness.castAndResolveInstant(player2, 0, replacement.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(5);
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new PlateArmor());
        armor.setAttachedTo(creature.getId());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        prepareOpponentTurn();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
