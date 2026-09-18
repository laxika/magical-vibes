package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.n.NemesisMask;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(equipment.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(equipment.getId()));
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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(equipment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(equipment.getId()));
    }
}
