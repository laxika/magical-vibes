package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
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

@CardUsed({ThorinMountainKing.class, ColossalDreadmaw.class, GrizzlyBears.class, LeoninScimitar.class})
class ThorinMountainKingTest extends BaseCardTest {

    @Test
    @DisplayName("Thorin attaches all targeted Equipment and deals damage once after at least one attachment")
    void attachesEquipmentThenDealsDamageOnce() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId(), firstEquipment.getId(), secondEquipment.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(victim.getId());
        assertThat(firstEquipment.getAttachedTo()).isEqualTo(host.getId());
        assertThat(secondEquipment.getAttachedTo()).isEqualTo(host.getId());

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Thorin can choose no Equipment and does not create the damage ability")
    void canChooseNoEquipment() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new ColossalDreadmaw());

        castThorin(List.of(host.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.getMarkedDamage()).isZero();
    }

    private void castThorin(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ThorinMountainKing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetIds);
    }
}
