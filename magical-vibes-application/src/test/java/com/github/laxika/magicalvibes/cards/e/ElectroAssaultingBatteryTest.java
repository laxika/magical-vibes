package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElectroAssaultingBattery.class, GrizzlyBears.class, Shock.class})
class ElectroAssaultingBatteryTest extends BaseCardTest {

    @Test
    @DisplayName("Preserves red mana across a step, but not other colors")
    void preservesRedManaAcrossStep() {
        harness.addToBattlefield(player1, new ElectroAssaultingBattery());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Casting an instant adds red mana")
    void castingInstantAddsRedMana() {
        harness.addToBattlefield(player1, new ElectroAssaultingBattery());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Leaving the battlefield lets its controller pay X to damage a player")
    void leavingTheBattlefieldDealsPaidDamageToPlayer() {
        Permanent electro = harness.addToBattlefieldAndReturn(player1, new ElectroAssaultingBattery());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, electro));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId(), player2.getId())
                .doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing X=0 deals no damage when Electro leaves")
    void choosingZeroDealsNoDamage() {
        Permanent electro = harness.addToBattlefieldAndReturn(player1, new ElectroAssaultingBattery());
        harness.setLife(player2, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, electro));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
