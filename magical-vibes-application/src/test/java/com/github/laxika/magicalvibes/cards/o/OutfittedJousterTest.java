package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BrawlersPlate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SteelclawLance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutfittedJouster.class, SteelclawLance.class, BrawlersPlate.class, Shock.class})
class OutfittedJousterTest extends BaseCardTest {

    @Test
    void conjuresAndAttachesBothEquipment() {
        Permanent jouster = castJouster();

        List<Permanent> equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Steelclaw Lance")
                        || permanent.getCard().getName().equals("Brawler's Plate"))
                .toList();

        assertThat(equipment).hasSize(2);
        assertThat(equipment).allMatch(permanent -> jouster.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void preventsDamageAndSacrificesOneAttachedEquipmentPerEvent() {
        Permanent jouster = castJouster();

        castShock(jouster);
        resolveEquipmentSacrifice("Brawler's Plate");
        assertThat(attachedEquipmentCount(jouster)).isEqualTo(1);
        assertThat(jouster.getMarkedDamage()).isZero();

        castShock(jouster);
        resolveEquipmentSacrifice("Steelclaw Lance");
        assertThat(attachedEquipmentCount(jouster)).isZero();
        assertThat(jouster.getMarkedDamage()).isZero();

        castShock(jouster);
        harness.assertInGraveyard(player1, "Outfitted Jouster");
    }

    @Test
    void equipmentSacrificeWaitsForTriggeredAbilityToResolve() {
        Permanent jouster = castJouster();

        castShock(jouster);

        assertThat(jouster.getMarkedDamage()).isZero();
        assertThat(attachedEquipmentCount(jouster)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void controllerCanChooseWhichEquipmentToSacrifice() {
        Permanent jouster = castJouster();

        castShock(jouster);
        resolveEquipmentSacrifice("Brawler's Plate");

        harness.assertInGraveyard(player1, "Brawler's Plate");
        harness.assertOnBattlefield(player1, "Steelclaw Lance");
        assertThat(attachedEquipmentCount(jouster)).isEqualTo(1);
    }

    @Test
    void stillConjuresEquipmentWhenJousterLeavesBeforeEnterTriggerResolves() {
        harness.castFromHand(player1, new OutfittedJouster(), "{2}{B}{R}");
        harness.passBothPriorities();
        Permanent jouster = findPermanent(player1, "Outfitted Jouster");

        castShock(jouster);
        harness.assertInGraveyard(player1, "Outfitted Jouster");
        resolveAllTriggers();

        Permanent lance = findPermanent(player1, "Steelclaw Lance");
        Permanent plate = findPermanent(player1, "Brawler's Plate");
        assertThat(lance.getAttachedTo()).isNull();
        assertThat(plate.getAttachedTo()).isNull();
    }

    private void resolveEquipmentSacrifice(String equipmentName) {
        Permanent equipment = findPermanent(player1, equipmentName);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, equipment.getId());
        }
    }

    private Permanent castJouster() {
        harness.castFromHand(player1, new OutfittedJouster(), "{2}{B}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Outfitted Jouster");
    }

    private void castShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private long attachedEquipmentCount(Permanent jouster) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> jouster.getId().equals(permanent.getAttachedTo()))
                .count();
    }
}
