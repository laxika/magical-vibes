package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronHillsStalwart.class, GrizzlyBears.class, LeoninScimitar.class})
class IronHillsStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("When Iron Hills Stalwart enters, it can attach a controlled Equipment to a controlled creature")
    void enteringAttachesEquipmentToControlledCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castStalwart();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("When Iron Hills Stalwart enters, its optional creature target can be declined")
    void enteringCanDeclineCreatureTarget() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castStalwart();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Iron Hills Stalwart's ability is not put on the stack without a controlled Equipment")
    void noControlledEquipmentMeansNoAbility() {
        castStalwart();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castStalwart() {
        harness.setHand(player1, List.of(new IronHillsStalwart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
