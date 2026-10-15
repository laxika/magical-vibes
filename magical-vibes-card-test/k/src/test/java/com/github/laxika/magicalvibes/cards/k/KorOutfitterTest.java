package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.t.TrustyMachete;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorOutfitter.class, TrustyMachete.class, CliffThreader.class, IntoTheRoil.class})
class KorOutfitterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability attaches an Equipment to a creature you control")
    void acceptingEtbAbilityAttachesEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffThreader());

        castKorOutfitter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the Equipment unattached")
    void decliningEtbAbilityDoesNotAttachEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffThreader());

        castKorOutfitter();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(creature.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability is not offered without a controlled Equipment")
    void noEquipmentMeansNoMayAbility() {
        castKorOutfitter();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castKorOutfitter() {
        harness.castFromHand(player1, new KorOutfitter(), "{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Kor Outfitter can attach Equipment to itself without paying equip costs")
    void canEquipItself() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());

        castKorOutfitter();
        harness.handlePermanentChosen(player1, equipment.getId());
        var outfitterId = harness.getPermanentId(player1, "Kor Outfitter");
        harness.handlePermanentChosen(player1, outfitterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(outfitterId);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the ability moves already attached Equipment to the chosen creature")
    void movesAlreadyAttachedEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent previousHost = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        Permanent newHost = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        equipment.setAttachedTo(previousHost.getId());

        castKorOutfitter();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, newHost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(newHost.getId());
    }

    @Test
    @DisplayName("Declining the ability preserves the Equipment's existing attachment")
    void decliningPreservesExistingAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent previousHost = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        Permanent newHost = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        equipment.setAttachedTo(previousHost.getId());

        castKorOutfitter();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, newHost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(equipment.getAttachedTo()).isEqualTo(previousHost.getId());
    }

    @Test
    @DisplayName("Both targets must be controlled by Kor Outfitter's controller")
    void targetChoicesExcludeOpponentsPermanents() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        Permanent opposingEquipment = harness.addToBattlefieldAndReturn(player2, new TrustyMachete());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CliffThreader());

        castKorOutfitter();
        var equipmentChoice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(equipmentChoice.validPermanentIds()).containsExactly(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        var creatureChoice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(creatureChoice.validPermanentIds())
                .contains(creature.getId(), harness.getPermanentId(player1, "Kor Outfitter"))
                .doesNotContain(equipment.getId(), opposingEquipment.getId(), opposingCreature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(opposingEquipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipment stays on its previous host if the chosen creature leaves before resolution")
    void creatureLeavingPreventsAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrustyMachete());
        Permanent previousHost = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        equipment.setAttachedTo(previousHost.getId());

        castKorOutfitter();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(previousHost.getId());
        harness.assertInHand(player1, "Cliff Threader");
        assertThat(gd.stack).isEmpty();
    }
}
