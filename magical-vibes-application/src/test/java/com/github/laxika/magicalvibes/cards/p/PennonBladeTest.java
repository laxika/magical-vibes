package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PennonBlade.class, NestInvader.class})
class PennonBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each creature the Equipment controller controls")
    void boostsForEachCreatureControlledByEquipmentController() {
        Permanent equippedCreature = addCreatureReady(player2, new NestInvader());
        Permanent otherCreature = addCreatureReady(player1, new NestInvader());
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        blade.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pennon Blade updates its bonus as the Equipment controller's creature count changes")
    void boostUpdatesWithCreatureCount() {
        Permanent equippedCreature = addCreatureReady(player1, new NestInvader());
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        blade.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);

        Permanent otherCreature = addCreatureReady(player1, new NestInvader());
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(otherCreature);
        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {4} attaches Pennon Blade to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent creature = addCreatureReady(player1, new NestInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent target = addCreatureReady(player2, new NestInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent target = addCreatureReady(player1, new NestInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWithOnlyThreeMana() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent target = addCreatureReady(player1, new NestInvader());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reequippingTransfersBonusOnlyOnResolution() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent original = addCreatureReady(player1, new NestInvader());
        Permanent target = addCreatureReady(player1, new NestInvader());
        blade.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void noBonusWhenEquipmentControllerControlsNoCreatures() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent creature = addCreatureReady(player2, new NestInvader());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void bonusUpdatesWhenEquipmentChangesController() {
        Permanent blade = addCreatureReady(player1, new PennonBlade());
        Permanent creature = addCreatureReady(player1, new NestInvader());
        addCreatureReady(player2, new NestInvader());
        addCreatureReady(player2, new NestInvader());
        blade.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(blade);
        gd.playerBattlefields.get(player2.getId()).add(blade);

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
