package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KozileksPredator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgresCleaver.class, KozileksPredator.class})
class OgresCleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Ogre's Cleaver to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent creature = addCreatureReady(player1, new KozileksPredator());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +5/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new KozileksPredator());
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ogre's Cleaver does not affect an unattached creature")
    void unattachedCleaverDoesNotAffectCreature() {
        Permanent creature = addCreatureReady(player1, new KozileksPredator());
        addCreatureReady(player1, new OgresCleaver());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void reequippingTransfersBoostOnlyOnResolution() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent original = addCreatureReady(player1, new KozileksPredator());
        Permanent target = addCreatureReady(player1, new KozileksPredator());
        cleaver.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(cleaver.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent target = addCreatureReady(player2, new KozileksPredator());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent target = addCreatureReady(player1, new KozileksPredator());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWithOnlyFourMana() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent target = addCreatureReady(player1, new KozileksPredator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cleaver.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void illegalTargetOnResolutionLeavesOriginalCreatureEquipped() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent original = addCreatureReady(player1, new KozileksPredator());
        Permanent target = addCreatureReady(player1, new KozileksPredator());
        cleaver.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostContinuesWhenEquippedCreatureChangesController() {
        Permanent cleaver = addCreatureReady(player1, new OgresCleaver());
        Permanent creature = addCreatureReady(player1, new KozileksPredator());
        cleaver.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
