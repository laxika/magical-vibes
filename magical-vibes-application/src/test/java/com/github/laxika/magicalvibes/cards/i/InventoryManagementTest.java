package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InventoryManagement.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class})
class InventoryManagementTest extends BaseCardTest {

    @Test
    @DisplayName("selects controlled Auras and Equipment and chooses a creature for each")
    void attachesEachSelectedPermanentToChosenCreature() {
        Permanent firstCreature = addCreature(player1);
        Permanent secondCreature = addCreature(player1);
        Permanent aura = addAura(player1, new HolyStrength(), firstCreature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), secondCreature);

        castInventoryManagement();

        PendingInteraction.MultiPermanentChoice attachments =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(attachments.validIds()).containsExactly(aura.getId(), equipment.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId(), equipment.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, firstCreature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("may choose not to attach any controlled Aura or Equipment")
    void mayChooseNone() {
        Permanent creature = addCreature(player1);
        Permanent aura = addAura(player1, new HolyStrength(), creature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), creature);

        castInventoryManagement();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castInventoryManagement() {
        harness.setHand(player1, List.of(new InventoryManagement()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addAura(Player owner, Card card, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(owner, card);
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private Permanent addEquipment(Player owner, Card card, Permanent host) {
        Permanent equipment = harness.addToBattlefieldAndReturn(owner, card);
        equipment.setAttachedTo(host.getId());
        return equipment;
    }
}
