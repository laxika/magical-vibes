package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MeldwebStrider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getName().equals("Phyrexian Goblin"))
                .hasSize(1);
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .filteredOn(permanent -> permanent.getCard().getName().equals("Phyrexian Goblin"))
                .hasSize(1);
    }

    @Test
    void cannotActivateWithoutAnOilCounterEventThisTurn() {
        Permanent reservoir = harness.addToBattlefieldAndReturn(player1, new ChurningReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(reservoir), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
