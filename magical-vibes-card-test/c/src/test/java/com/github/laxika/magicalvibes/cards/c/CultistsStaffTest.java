package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CultistsStaff.class, GrizzlyBears.class})
class CultistsStaffTest extends BaseCardTest {

    @Test
    void resolvingEquipAttachesToTargetCreature() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equippedCreatureGetsPlusTwoPlusTwo() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        staff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void equippedCreatureLosesBoostWhenStaffIsRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        staff.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(staff);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canReEquipToAnotherCreature() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        staff.setAttachedTo(creature1.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.getEffectivePower(gd, creature1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature2)).isEqualTo(4);
    }

    @Test
    void unattachedStaffDoesNotBoostCreatures() {
        addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equipRequiresThreeMana() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, staff.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(staff.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedWithNonemptyStack() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(staff.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void failedReequipPreservesOriginalAttachmentAndBoost() {
        Permanent staff = addCreatureReady(player1, new CultistsStaff());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        staff.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(staff.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(staff.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
