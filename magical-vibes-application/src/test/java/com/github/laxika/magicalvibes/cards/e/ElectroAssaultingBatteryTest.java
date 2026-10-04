package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PumpkinBombardment;
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

@CardUsed({ElectroAssaultingBattery.class, PumpkinBombardment.class, Shock.class})
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
    @DisplayName("Casting a sorcery adds red mana before the spell resolves")
    void castingSorceryAddsRedMana() {
        Permanent electro = harness.addToBattlefieldAndReturn(player1, new ElectroAssaultingBattery());
        harness.setHand(player1, List.of(new PumpkinBombardment()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, electro.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Electro, Assaulting Battery");
    }

    @Test
    @DisplayName("An opponent's instant does not add mana for Electro's controller")
    void opponentsInstantDoesNotAddMana() {
        harness.addToBattlefield(player1, new ElectroAssaultingBattery());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Electro")
    void creatureSpellDoesNotAddMana() {
        harness.addToBattlefield(player1, new ElectroAssaultingBattery());
        harness.setHand(player1, List.of(new ElectroAssaultingBattery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Electro does not preserve the opponent's red mana")
    void doesNotPreserveOpponentsMana() {
        harness.addToBattlefield(player1, new ElectroAssaultingBattery());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Leaving the battlefield lets its controller pay X to damage a player")
    void leavingTheBattlefieldDealsPaidDamageToPlayer() {
        Permanent electro = harness.addToBattlefieldAndReturn(player1, new ElectroAssaultingBattery());
        Permanent otherElectro = harness.addToBattlefieldAndReturn(player2, new ElectroAssaultingBattery());
        harness.setLife(player2, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, electro));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 3);
        harness.assertLife(player2, 20);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId(), player2.getId())
                .doesNotContain(otherElectro.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
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
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.handleXValueChosen(player1, 0);
        harness.assertLife(player2, 20);
    }
}
