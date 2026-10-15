package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagneticTheft.class, EnsouledScimitar.class, Arachnoid.class, AvariceTotem.class})
class MagneticTheftTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches any target Equipment to any target creature")
    void attachesEquipmentToCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        castMagneticTheft(equipment, creature);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Moves an Equipment already attached to another creature")
    void movesAlreadyAttachedEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        equipment.setAttachedTo(oldCreature.getId());

        castMagneticTheft(equipment, newCreature);

        assertThat(equipment.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Cannot target a non-Equipment permanent as the first target")
    void cannotTargetNonEquipment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AvariceTotem());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Equipment");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent as the second target")
    void cannotTargetNoncreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(equipment.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Animated Equipment remains a legal target but cannot become attached")
    void cannotAttachAnimatedEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        castMagneticTheft(equipment, creature);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magnetic Theft");
    }

    @Test
    @DisplayName("Can attach Equipment to an animated Equipment creature")
    void canAttachToAnimatedEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        castMagneticTheft(equipment, creature);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Attaching to the current equipped creature does not change the timestamp")
    void attachingToSameCreatureDoesNothing() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        castMagneticTheft(equipment, creature);
        long timestamp = equipment.getTimestamp();

        castMagneticTheft(equipment, creature);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Equipment stays on its old creature when the new target leaves before resolution")
    void missingCreatureDoesNotUnattachEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new EnsouledScimitar());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        equipment.setAttachedTo(oldCreature.getId());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(equipment.getId(), newCreature.getId()));
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, newCreature);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magnetic Theft");
    }

    private void castMagneticTheft(Permanent equipment, Permanent creature) {
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(equipment.getId(), creature.getId()));
    }
}
