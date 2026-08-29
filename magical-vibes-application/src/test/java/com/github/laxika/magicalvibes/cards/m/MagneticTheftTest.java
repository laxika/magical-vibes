package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagneticTheftTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches any target Equipment to any target creature")
    void attachesEquipmentToCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new StriderHarness());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMagneticTheft(equipment, creature);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Cannot target a non-Equipment permanent as the first target")
    void cannotTargetNonEquipment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new StriderHarness());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Equipment");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent as the second target")
    void cannotTargetNoncreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new StriderHarness());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(equipment.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castMagneticTheft(Permanent equipment, Permanent creature) {
        harness.setHand(player1, List.of(new MagneticTheft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();
    }
}
