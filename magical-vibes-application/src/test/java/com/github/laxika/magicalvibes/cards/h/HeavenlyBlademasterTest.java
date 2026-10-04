package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeavenlyBlademaster.class, GrizzlyBears.class, HolyStrength.class, LeoninScimitar.class})
class HeavenlyBlademasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers controlled Auras and Equipment and boosts other creatures per attachment")
    void attachesControlledAurasAndEquipmentAndBoostsOtherCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(player1, new HolyStrength(), ownCreature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), ownCreature);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentEquipment = addEquipment(player2, new LeoninScimitar(), opponentCreature);

        Permanent blademaster = castBlademaster();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(aura.getId(), equipment.getId())
                .doesNotContain(opponentEquipment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId(), equipment.getId()));

        assertThat(aura.getAttachedTo()).isEqualTo(blademaster.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(blademaster.getId());
        assertThat(opponentEquipment.getAttachedTo()).isEqualTo(opponentCreature.getId());
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB attachment choice may choose none")
    void attachmentChoiceMayChooseNone() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(player1, new HolyStrength(), ownCreature);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), ownCreature);

        castBlademaster();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(aura.getAttachedTo()).isEqualTo(ownCreature.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(ownCreature.getId());
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
    }

    @Test
    void canChooseOnlyUnattachedEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(player1, new HolyStrength(), creature);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        Permanent blademaster = castBlademaster();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(blademaster.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    void noAttachmentsNeedsNoChoice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castBlademaster();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void countsOpposingAurasAndUpdatesWhenAttachmentsMove() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new HeavenlyBlademaster());
        Permanent aura = addAura(player2, new HolyStrength(), blademaster);
        Permanent equipment = addEquipment(player1, new LeoninScimitar(), blademaster);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(9);

        equipment.setAttachedTo(null);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        aura.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(6);
    }

    private Permanent castBlademaster() {
        harness.castFromHand(player1, new HeavenlyBlademaster(), "{5}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Heavenly Blademaster");
    }

    private Permanent addAura(Player owner, Card auraCard, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(owner, auraCard);
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private Permanent addEquipment(Player owner, Card equipmentCard, Permanent host) {
        Permanent equipment = harness.addToBattlefieldAndReturn(owner, equipmentCard);
        equipment.setAttachedTo(host.getId());
        return equipment;
    }
}
