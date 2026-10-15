package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MeldwebStrider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChurningReservoir.class, GrizzlyBears.class, AccordersShield.class,
        MeldwebStrider.class, CullingDais.class, CopperLonglegs.class})
class ChurningReservoirTest extends BaseCardTest {

    @Test
    void upkeepPutsOilCounterOnAnotherNontokenArtifactOrCreatureYouControl() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), shield.getId())
                .doesNotContain(reservoir.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(shield.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void createsPhyrexianGoblinAfterOilCounterIsRemovedFromControlledPermanent() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new MeldwebStrider());
        strider.setCounterCount(CounterType.OIL, 1);

        harness.activateAbility(player1, battlefieldIndex(strider), 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .hasSize(1).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void createsPhyrexianGoblinAfterOilPermanentIsPutIntoGraveyard() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent dais = harness.addToBattlefieldAndReturn(player1, new CullingDais());
        dais.setCounterCount(CounterType.OIL, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dais), 1, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .hasSize(1).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotActivateWithoutAnOilCounterEventThisTurn() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentRemovingOilDoesNotEnableActivation() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent strider = harness.addToBattlefieldAndReturn(player2, new MeldwebStrider());
        strider.setCounterCount(CounterType.OIL, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
    }

    @Test
    void opponentOilPermanentDyingEnablesActivation() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        spider.setCounterCount(CounterType.OIL, 1);
        spider.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Copper Longlegs");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(1);
    }

    @Test
    void upkeepCannotTargetGoblinToken() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new MeldwebStrider());
        harness.addToBattlefield(player1, new MeldwebStrider());
        strider.setCounterCount(CounterType.OIL, 1);
        harness.activateAbility(player1, battlefieldIndex(strider), 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Phyrexian Goblin");

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(strider.getId()).doesNotContain(token.getId());
        harness.handlePermanentChosen(player1, strider.getId());
        harness.passBothPriorities();
        assertThat(token.getCounterCount(CounterType.OIL)).isZero();
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void oilRemovalFromPreviousTurnDoesNotEnableActivation() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new MeldwebStrider());
        strider.setCounterCount(CounterType.OIL, 1);
        harness.activateAbility(player1, battlefieldIndex(strider), 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permanentDyingWithoutOilDoesNotEnableActivation() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        spider.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Copper Longlegs");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void oilPermanentDyingOnPreviousTurnDoesNotEnableActivation() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        spider.setCounterCount(CounterType.OIL, 1);
        spider.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Copper Longlegs");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentUpkeepDoesNotPutOilOnControlledPermanent() {
        harness.addToBattlefield(player1, new ChurningReservoir());
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new MeldwebStrider());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
