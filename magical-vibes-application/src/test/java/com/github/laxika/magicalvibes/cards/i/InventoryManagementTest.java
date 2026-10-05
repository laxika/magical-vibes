package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InventoryManagement.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class,
        SolRing.class, TrollAscetic.class, PincherBeetles.class})
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

    @Test
    void mayMoveOnlyOneAttachmentAndExcludeOpposingPermanents() {
        Permanent firstCreature = addCreature(player1);
        Permanent secondCreature = addCreature(player1);
        Permanent opposingCreature = addCreature(player2);
        Permanent aura = addAura(player1, new HolyStrength(), firstCreature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), firstCreature);
        Permanent opposingEquipment = addEquipment(player2, new LeoninScimitar(), opposingCreature);

        castInventoryManagement();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(aura.getId(), equipment.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(opposingEquipment.getAttachedTo()).isEqualTo(opposingCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attachesUnattachedEquipmentWithoutPayingEquipCost() {
        Permanent creature = addCreature(player1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castInventoryManagement();
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosingCurrentHostDoesNotReattachPermanent() {
        Permanent creature = addCreature(player1);
        Permanent aura = addAura(player1, new HolyStrength(), creature);
        long originalTimestamp = aura.getTimestamp();

        castInventoryManagement();
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(aura.getTimestamp()).isEqualTo(originalTimestamp);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithoutAttachments() {
        addCreature(player1);

        castInventoryManagement();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Inventory Management");
    }

    @Test
    void leavesEquipmentUnattachedWhenNoControlledCreatureExists() {
        addCreature(player2);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castInventoryManagement();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Inventory Management");
    }

    @Test
    void attachesAuraAndEquipmentToCreatureWithShroud() {
        Permanent firstCreature = addCreature(player1);
        Permanent beetles = addCreatureReady(player1, new PincherBeetles());
        Permanent aura = addAura(player1, new HolyStrength(), firstCreature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), firstCreature);

        castInventoryManagement();
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId(), equipment.getId()));
        harness.handlePermanentChosen(player1, beetles.getId());
        harness.handlePermanentChosen(player1, beetles.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(beetles.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(beetles.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void splitSecondPreventsResponsesAndNonManaAbilities() {
        Permanent troll = addCreatureReady(player2, new TrollAscetic());
        harness.setHand(player2, List.of(new InventoryManagement()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new InventoryManagement(), "{R}{W}");

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
        assertThat(troll.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.castFromHand(player2, new InventoryManagement(), "{R}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Inventory Management");
    }

    @Test
    void splitSecondAllowsManaAbilities() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.castFromHand(player1, new InventoryManagement(), "{R}{W}");

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Inventory Management");
    }

    private void castInventoryManagement() {
        harness.castFromHand(player1, new InventoryManagement(), "{R}{W}");
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
