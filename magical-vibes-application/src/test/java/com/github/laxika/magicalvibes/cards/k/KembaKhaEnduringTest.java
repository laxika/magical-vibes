package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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

    private Permanent addKembaReady(Player player) {
        Permanent kemba = new Permanent(new KembaKhaEnduring());
        kemba.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(kemba);
        return kemba;
    }

    private Permanent addEquipment(Player player, Permanent host) {
        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setSummoningSick(false);
        if (host != null) {
            equipment.setAttachedTo(host.getId());
        }
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }

    private void addManaForTokenAbility(Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
