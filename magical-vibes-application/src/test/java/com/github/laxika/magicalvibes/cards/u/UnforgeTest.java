package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnforgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Equipment and deals 2 damage to its attached creature")
    void destroysEquipmentAndDamagesAttachedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals no damage when target Equipment is unattached")
    void unattachedEquipmentDoesNotDealDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Two damage destroys a 2/2 creature wearing the Equipment")
    void damageCanDestroyAttachedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unforge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be an Equipment");
    }

    private void castUnforge(Permanent equipment) {
        harness.setHand(player1, List.of(new Unforge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, equipment.getId());
        harness.passBothPriorities();
    }
}
