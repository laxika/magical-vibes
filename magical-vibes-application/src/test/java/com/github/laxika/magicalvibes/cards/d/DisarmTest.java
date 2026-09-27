package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disarm.class, Frogmite.class, LeoninScimitar.class})
class DisarmTest extends BaseCardTest {

    @Test
    @DisplayName("Unattaches all Equipment from target creature")
    void unattachesAllEquipmentFromTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Frogmite());
        Permanent equipment1 = addEquipment(player2, target);
        Permanent equipment2 = addEquipment(player2, target);
        harness.setHand(player1, List.of(new Disarm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(equipment1.getAttachedTo()).isNull();
        assertThat(equipment2.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, equipment1, equipment2);
    }

    @Test
    @DisplayName("Does not unattach Equipment from another creature")
    void doesNotUnattachEquipmentFromAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Frogmite());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new Frogmite());
        Permanent equipment = addEquipment(player2, otherCreature);
        harness.setHand(player1, List.of(new Disarm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    @Test
    @DisplayName("Unattaches Equipment regardless of who controls it")
    void unattachesEquipmentControlledByAnotherPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Frogmite());
        Permanent equipment = addEquipment(player1, target);
        harness.setHand(player1, List.of(new Disarm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new Disarm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature");
    }

    private Permanent addEquipment(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        equipment.setAttachedTo(target.getId());
        return equipment;
    }
}
