package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.n.NemesisMask;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarryAway.class, ArcboundWorker.class, NemesisMask.class})
class CarryAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Carry Away cannot target a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Equipment");
    }

    @Test
    @DisplayName("Carry Away takes control of enchanted Equipment and unattaches it")
    void takesControlAndUnattachesEquipment() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        Permanent equipment = harness.enterBattlefieldAndReturn(player2, new NemesisMask());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.passBothPriorities());

        harness.assertOnBattlefield(player1, "Nemesis Mask");
        harness.assertNotOnBattlefield(player2, "Nemesis Mask");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nemesis Mask");
        harness.assertNotOnBattlefield(player2, "Nemesis Mask");
        assertThat(equipment.getAttachedTo()).isNull();

        Permanent aura = findPermanent(player1, "Carry Away");
        assertThat(aura.getAttachedTo()).isEqualTo(equipment.getId());
    }

    @Test
    @DisplayName("Carry Away returns the Equipment to its previous controller when it leaves")
    void returnsEquipmentWhenAuraLeavesBattlefield() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        Permanent equipment = harness.enterBattlefieldAndReturn(player2, new NemesisMask());
        equipment.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Carry Away");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        harness.assertOnBattlefield(player2, "Nemesis Mask");
        harness.assertNotOnBattlefield(player1, "Nemesis Mask");
    }

    @Test
    @DisplayName("Carry Away takes control of Equipment that is not attached")
    void takesControlOfUnattachedEquipment() {
        Permanent equipment = harness.enterBattlefieldAndReturn(player2, new NemesisMask());
        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, equipment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nemesis Mask");
        harness.assertNotOnBattlefield(player2, "Nemesis Mask");
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(findPermanent(player1, "Carry Away").getAttachedTo()).isEqualTo(equipment.getId());
    }

    @Test
    @CardUsed(EchoingTruth.class)
    @DisplayName("Carry Away's trigger still unattaches Equipment after the Aura leaves")
    void unattachesEquipmentAfterAuraLeavesBeforeTriggerResolves() {
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new ArcboundWorker());
        Permanent equipment = harness.enterBattlefieldAndReturn(player2, new NemesisMask());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new CarryAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, equipment.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        Permanent aura = findPermanent(player1, "Carry Away");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        harness.setHand(player2, List.of(new EchoingTruth(), new EchoingTruth()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.castAndResolveInstant(player2, 0, aura.getId()));

        harness.assertInHand(player1, "Carry Away");
        harness.assertOnBattlefield(player2, "Nemesis Mask");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player2, "Nemesis Mask");
    }
}
