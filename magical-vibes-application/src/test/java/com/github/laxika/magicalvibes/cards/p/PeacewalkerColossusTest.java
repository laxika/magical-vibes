package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeacewalkerColossusTest extends BaseCardTest {

    @Test
    void animatesAnotherVehicleYouControlUntilEndOfTurn() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        addAnimationMana();

        harness.activateAbility(player1, 0, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, colossus)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void cannotAnimateItselfOrAnOpponentVehicle() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        addAnimationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, colossus.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vehicle you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentVehicle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vehicle you control");
    }

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastFour() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addReadyPeacewalkerColossus(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addReadyPeacewalkerColossus(Player player) {
        Permanent colossus = new Permanent(new PeacewalkerColossus());
        colossus.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(colossus);
        return colossus;
    }

    private void addAnimationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
