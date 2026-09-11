package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WizardsStaff;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwalinWeaponmaster.class, WizardsStaff.class, GrizzlyBears.class})
class DwalinWeaponmasterTest extends BaseCardTest {

    @Test
    void entersHonesEachEquipmentYouControl() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent attachedStaff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        Permanent unattachedStaff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        Permanent opponentStaff = harness.addToBattlefieldAndReturn(player2, new WizardsStaff());
        attachedStaff.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new DwalinWeaponmaster()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attachedStaff.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(unattachedStaff.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(opponentStaff.getCounterCount(CounterType.HONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    void attackHonesEquipmentAndTheCounterKeepsBoostingAfterDwalinLeaves() {
        Permanent dwalin = addCreatureReady(player1, new DwalinWeaponmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new WizardsStaff());
        staff.setAttachedTo(bears.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(staff.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dwalin));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }
}
