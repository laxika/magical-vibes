package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KembaKhaEnduring.class, GrizzlyBears.class, LeoninScimitar.class, Conspiracy.class})
class KembaKhaEnduringTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creatures you control get +1/+1, including Kemba")
    void equippedCreaturesYouControlGetBoost() {
        Permanent kemba = addKembaReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent kembaEquipment = addEquipment(player1, kemba);
        Permanent bearsEquipment = addEquipment(player1, bears);

        assertThat(gqs.getEffectivePower(gd, kemba)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kemba)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(kembaEquipment.getAttachedTo()).isEqualTo(kemba.getId());
        assertThat(bearsEquipment.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Kemba's Cat trigger attaches a target Equipment to the entering Cat")
    void catTriggerAttachesEquipmentToEnteringCat() {
        addKembaReady(player1);
        Permanent equipment = addEquipment(player1, null);
        addManaForTokenAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(equipment.getId());

        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        Permanent cat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.CAT))
                .findFirst()
                .orElseThrow();
        assertThat(equipment.getAttachedTo()).isEqualTo(cat.getId());
    }

    @Test
    @DisplayName("The Cat trigger can be declined")
    void catTriggerCanBeDeclined() {
        addKembaReady(player1);
        Permanent equipment = addEquipment(player1, null);
        addManaForTokenAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The Cat trigger does not trigger for a non-Cat creature")
    void nonCatDoesNotTrigger() {
        addKembaReady(player1);
        Permanent equipment = addEquipment(player1, null);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The Cat trigger does nothing if the target Equipment leaves before resolution")
    void triggerDoesNothingIfEquipmentLeaves() {
        addKembaReady(player1);
        Permanent equipment = addEquipment(player1, null);
        addManaForTokenAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Kemba creates a 2/2 white Cat token for five mana")
    void createsCatToken() {
        addKembaReady(player1);
        addManaForTokenAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent cat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getSubtypes().contains(CardSubtype.CAT))
                .findFirst()
                .orElseThrow();
        assertThat(cat.getCard().getPower()).isEqualTo(2);
        assertThat(cat.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void ownEntryAttachesEquipment() {
        Permanent equipment = addEquipment(player1, null);
        harness.castFromHand(player1, new KembaKhaEnduring(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Kemba, Kha Enduring").getId());
    }

    @Test
    void ownEntryStillTriggersWhenKembaIsNotACat() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.HUMAN.name());
        Permanent equipment = addEquipment(player1, null);

        harness.castFromHand(player1, new KembaKhaEnduring(), "{1}{W}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(equipment.getId());
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Kemba, Kha Enduring").getId());
    }

    @Test
    void triggerOnlyOffersEquipmentYouControl() {
        addKembaReady(player1);
        Permanent ownEquipment = addEquipment(player1, null);
        Permanent opposingEquipment = addEquipment(player2, null);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        addManaForTokenAbility(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownEquipment.getId())
                .doesNotContain(opposingEquipment.getId(), otherCreature.getId());
        harness.handlePermanentChosen(player1, ownEquipment.getId());
        harness.passBothPriorities();
    }

    @Test
    void equipmentMovesFromPreviousCreatureToEnteringCat() {
        Permanent kemba = addKembaReady(player1);
        Permanent equipment = addEquipment(player1, kemba);
        addManaForTokenAbility(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(equipment.getAttachedTo()).isEqualTo(cat.getId());
        assertThat(gqs.getEffectivePower(gd, kemba)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
    }

    @Test
    void abilityStillAttachesAfterKembaLeaves() {
        Permanent kemba = addKembaReady(player1);
        Permanent equipment = addEquipment(player1, null);
        addManaForTokenAbility(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(kemba);
        harness.passBothPriorities();

        Permanent cat = findPermanent(player1, "Cat");
        assertThat(equipment.getAttachedTo()).isEqualTo(cat.getId());
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
    }

    @Test
    void equipmentStaysOnOldHostWhenEnteringCatLeaves() {
        Permanent kemba = addKembaReady(player1);
        Permanent equipment = addEquipment(player1, kemba);
        addManaForTokenAbility(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, equipment.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Cat"));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(kemba.getId());
    }

    @Test
    void boostDoesNotApplyToUnequippedOrOpposingCreatures() {
        Permanent kemba = addKembaReady(player1);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        addEquipment(player2, opposingBears);

        assertThat(gqs.getEffectivePower(gd, kemba)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(3);
    }

    private Permanent addKembaReady(Player player) {
        return addCreatureReady(player, new KembaKhaEnduring());
    }

    private Permanent addEquipment(Player player, Permanent host) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        equipment.setSummoningSick(false);
        if (host != null) {
            equipment.setAttachedTo(host.getId());
        }
        return equipment;
    }

    private void addManaForTokenAbility(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
