package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AettirAndPriwen.class, GrizzlyBears.class})
class AettirAndPriwenTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness equal to its Equipment controller's life total")
    void setsEquippedCreatureBasePowerAndToughnessToControllerLife() {
        harness.setLife(player1, 14);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);
    }

    @Test
    @DisplayName("Equipped creature's base power and toughness update with life total changes")
    void updatesWhenControllerLifeChanges() {
        harness.setLife(player1, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(creature.getId());

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(21);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(21);

        harness.setLife(player1, 8);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    @DisplayName("Unattached Equipment does not set a creature's base power and toughness")
    void doesNothingWhileUnattached() {
        harness.setLife(player1, 14);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
    @Test
    @DisplayName("Equip pays five generic mana and moves the base power/toughness effect to the new creature")
    void equipPaysFiveAndMovesEffect() {
        harness.setLife(player1, 14);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 2, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(equipment.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(14);
    }

    @Test
    @DisplayName("Equipment uses its own controller's life when attached to an opponent's creature")
    void usesEquipmentControllerLifeAcrossBattlefields() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 7);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AettirAndPriwen());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);

        harness.setLife(player2, 3);
        harness.setLife(player1, 9);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        harness.addToBattlefield(player1, new AettirAndPriwen());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip can only be activated at sorcery speed")
    void equipRejectsCombatTiming() {
        harness.addToBattlefield(player1, new AettirAndPriwen());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
