package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethersquallAncient.class, GrizzlyBears.class, SerraAngel.class, GloriousAnthem.class})
class AethersquallAncientTest extends BaseCardTest {

    @Test
    void gainsThreeEnergyAtBeginningOfUpkeep() {
        addCreatureReady(player1, new AethersquallAncient());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEightEnergyToReturnAllOtherCreatures() {
        Permanent ancient = addCreatureReady(player1, new AethersquallAncient());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player1, new GloriousAnthem());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ancient);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Serra Angel");
    }

    @Test
    void cannotActivateWithoutEightEnergy() {
        addCreatureReady(player1, new AethersquallAncient());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");
    }

    @Test
    void canActivateOnlyAtSorcerySpeed() {
        addCreatureReady(player1, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGainEnergyDuringOpponentsUpkeep() {
        addCreatureReady(player1, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
    }

    @Test
    void multipleAncientsEachGiveTheirControllerEnergy() {
        addCreatureReady(player1, new AethersquallAncient());
        addCreatureReady(player1, new AethersquallAncient());
        addCreatureReady(player2, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyImmediatelyAndReturnsOtherAncientsButNotSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AethersquallAncient());
        harness.addToBattlefield(player1, new AethersquallAncient());
        harness.addToBattlefield(player2, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 11);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Aethersquall Ancient");

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        harness.assertNotOnBattlefield(player2, "Aethersquall Ancient");
        harness.assertInHand(player1, "Aethersquall Ancient");
        harness.assertInHand(player2, "Aethersquall Ancient");
    }

    @Test
    void cannotActivateWithAnotherAbilityOnStack() {
        addCreatureReady(player1, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 16);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aethersquall Ancient");
    }

    @Test
    void cannotActivateDuringOwnUpkeepEvenWithEnoughEnergy() {
        addCreatureReady(player1, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(8);
    }

    @Test
    void sevenEnergyIsInsufficientAndIsNotSpent() {
        addCreatureReady(player1, new AethersquallAncient());
        gd.playerEnergyCounters.put(player1.getId(), 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
        harness.assertOnBattlefield(player1, "Aethersquall Ancient");
    }

    @Test
    void returnsCreatureToOwnerRatherThanItsCurrentController() {
        Permanent source = addCreatureReady(player1, new AethersquallAncient());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new AethersquallAncient());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        harness.assertInHand(player2, "Aethersquall Ancient");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(stolen.getCard());
    }
}
