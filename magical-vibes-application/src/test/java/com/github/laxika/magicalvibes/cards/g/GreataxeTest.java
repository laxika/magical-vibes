package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Greataxe.class, DireWolfProwler.class})
class GreataxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Greataxe gives the creature +4/+0")
    void equippingGivesPowerBoost() {
        Permanent greataxe = addCreatureReady(player1, new Greataxe());
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(greataxe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unequipped creature does not get Greataxe's boost")
    void unequippedCreatureIsNotBoosted() {
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        addCreatureReady(player1, new Greataxe());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Greataxe cannot equip an opponent's creature")
    void cannotEquipOpponentCreature() {
        addCreatureReady(player1, new Greataxe());
        Permanent creature = addCreatureReady(player2, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Greataxe cannot equip with only four mana")
    void cannotEquipWithoutFiveMana() {
        Permanent greataxe = addCreatureReady(player1, new Greataxe());
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(greataxe.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reequipping moves the power boost to the new creature")
    void reequippingMovesBoost() {
        Permanent greataxe = addCreatureReady(player1, new Greataxe());
        Permanent first = addCreatureReady(player1, new DireWolfProwler());
        Permanent second = addCreatureReady(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(greataxe.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(greataxe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Greataxe cannot equip during combat")
    void cannotEquipDuringCombat() {
        addCreatureReady(player1, new Greataxe());
        Permanent creature = addCreatureReady(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gd.currentStep = TurnStep.BEGINNING_OF_COMBAT;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
