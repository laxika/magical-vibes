package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.g.GateSmasher;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SagesReverie;
import com.github.laxika.magicalvibes.cards.t.TormodTheDesecrator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnfinishedBusiness.class, AbundantGrowth.class, GrizzlyBears.class,
        GateSmasher.class, HolyStrength.class, LeoninScimitar.class, SagesReverie.class,
        TormodTheDesecrator.class})
class UnfinishedBusinessTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the creature when no attachments are chosen")
    void returnsCreatureWithoutSelectingAttachments() {
        GrizzlyBears creature = new GrizzlyBears();
        HolyStrength aura = new HolyStrength();
        cast(List.of(creature, aura), List.of(creature));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Unfinished Business");
    }

    @Test
    @DisplayName("A returned Aura's enter ability triggers")
    void returnedAuraTriggersItsEnterAbility() {
        GrizzlyBears creature = new GrizzlyBears();
        SagesReverie aura = new SagesReverie();
        HolyStrength otherAura = new HolyStrength();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        cast(List.of(creature, aura, otherAura), List.of(creature, aura, otherAura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Sage's Reverie").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Grizzly Bears").getId());
    }

    @Test
    @DisplayName("Without the creature, the Equipment still returns unattached and the Aura stays in the graveyard")
    void missingCreatureTargetStillReturnsEquipmentButLeavesAuraInGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, aura, equipment));
        harness.setHand(player1, List.of(new UnfinishedBusiness()));
        addMana();
        harness.castSorcery(player1, 0, List.of(creature.getId(), aura.getId(), equipment.getId()));
        harness.setGraveyard(player1, List.of(aura, equipment));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Strength");
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returns the creature with the chosen Aura and Equipment attached to it")
    void returnsCreatureAndAttachesSelectedAurasAndEquipment() {
        GrizzlyBears creature = new GrizzlyBears();
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        cast(List.of(creature, aura, equipment), List.of(creature, aura, equipment));

        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo())
                .isEqualTo(returnedCreature.getId());
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo())
                .isEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("An Equipment that can't be attached enters unattached")
    void equipmentEntersUnattachedWhenItCannotAttach() {
        GrizzlyBears creature = new GrizzlyBears();
        GateSmasher equipment = new GateSmasher();
        cast(List.of(creature, equipment), List.of(creature, equipment));

        assertThat(findPermanent(player1, "Gate Smasher").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An Aura that can't enchant the creature stays in the graveyard")
    void auraThatCannotEnchantReturnedCreatureStaysInGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        AbundantGrowth aura = new AbundantGrowth();
        cast(List.of(creature, aura), List.of(creature, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getId());
        assertThat(findPermanents(player1, "Abundant Growth")).isEmpty();
    }

    @Test
    @DisplayName("A card that is neither an Aura nor an Equipment can't be the second target")
    void rejectsNonAuraAndNonEquipmentAttachmentTarget() {
        GrizzlyBears creature = new GrizzlyBears();
        GrizzlyBears invalidAttachment = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, invalidAttachment));
        harness.setHand(player1, List.of(new UnfinishedBusiness()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), invalidAttachment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature returns first, then its Aura and Equipment return together as one event")
    void attachmentsLeaveTheGraveyardTogetherAfterTheCreature() {
        harness.addToBattlefield(player1, new TormodTheDesecrator());
        GrizzlyBears creature = new GrizzlyBears();
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        cast(List.of(creature, aura, equipment), List.of(creature, aura, equipment));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(returnedCreature.getId());
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isEqualTo(returnedCreature.getId());
    }

    private void cast(List<Card> graveyard, List<Card> targets) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new UnfinishedBusiness()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targets.stream().map(Card::getId).toList());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
