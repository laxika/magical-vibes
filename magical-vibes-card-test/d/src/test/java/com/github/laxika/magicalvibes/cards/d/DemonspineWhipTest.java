package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.m.MagneticTheft;
import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonspineWhip.class, GrizzledLeotau.class, QasaliPridemage.class, MagneticTheft.class})
class DemonspineWhipTest extends BaseCardTest {

    // The whip is always added at battlefield index 0 (permanentIndex), the creature at index 1.
    // Ability index 0 = "{X}: +X/+0", ability index 1 = "Equip {1}".


    @Test
    @DisplayName("Activating the pump ability gives the equipped creature +X/+0")
    void pumpBoostsEquippedCreature() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        whip.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        whip.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The pump does nothing when the Equipment is not attached to a creature")
    void pumpDoesNothingWhenUnattached() {
        addCreatureReady(player1, new DemonspineWhip()); // present but unattached
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }


    @Test
    @DisplayName("Resolving equip attaches the Equipment to the target creature")
    void resolvingEquipAttaches() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(whip.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void zeroXIsLegalWithoutMana() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        whip.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player2, new GrizzledLeotau());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedActivationsAccumulateIndependently() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent creature = addCreatureReady(player1, new GrizzledLeotau());
        whip.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, 2, null);
        harness.activateAbility(player1, 0, 0, 3, null);
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(5);
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void pumpUsesCreatureEquippedAtResolution() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent original = addCreatureReady(player1, new GrizzledLeotau());
        Permanent replacement = addCreatureReady(player2, new GrizzledLeotau());
        whip.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player1, List.of(new MagneticTheft()));

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.castAndResolveInstant(player1, 0, List.of(whip.getId(), replacement.getId()));
        assertThat(whip.getAttachedTo()).isEqualTo(replacement.getId());
        harness.passBothPriorities();

        assertThat(original.getPowerModifier()).isZero();
        assertThat(replacement.getPowerModifier()).isEqualTo(3);
        assertThat(replacement.getToughnessModifier()).isZero();
    }

    @Test
    void destroyedWhipUsesItsLastAttachmentRatherThanItsActivationAttachment() {
        Permanent whip = addCreatureReady(player1, new DemonspineWhip());
        Permanent original = addCreatureReady(player1, new GrizzledLeotau());
        Permanent replacement = addCreatureReady(player1, new GrizzledLeotau());
        addCreatureReady(player2, new QasaliPridemage());
        whip.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MagneticTheft()));

        harness.activateAbility(player1, 0, 0, 3, null);
        harness.castAndResolveInstant(player1, 0, List.of(whip.getId(), replacement.getId()));
        assertThat(whip.getAttachedTo()).isEqualTo(replacement.getId());
        harness.activateAbility(player2, 0, 0, null, whip.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Demonspine Whip");
        harness.passBothPriorities();

        assertThat(original.getPowerModifier()).isZero();
        assertThat(replacement.getPowerModifier()).isEqualTo(3);
        assertThat(replacement.getToughnessModifier()).isZero();
    }
}
