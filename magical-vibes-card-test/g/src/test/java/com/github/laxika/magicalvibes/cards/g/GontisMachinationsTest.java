package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({GontisMachinations.class, Shock.class})
class GontisMachinationsTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets energy the first time its controller loses life each turn")
    void getsEnergyOnFirstLifeLossEachTurn() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again on the first life loss of a later turn")
    void getsEnergyAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Pays two energy and sacrifices itself to drain each opponent")
    void paysEnergyAndSacrificesToDrainOpponents() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Gonti's Machinations");
        harness.assertInGraveyard(player1, "Gonti's Machinations");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Cannot activate without two energy counters")
    void cannotActivateWithoutTwoEnergyCounters() {
        Permanent machinations = harness.addToBattlefieldAndReturn(player1, new GontisMachinations());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two energy counters");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(machinations);
    }

    @Test
    @DisplayName("Does not trigger if its controller already lost life before it entered")
    void doesNotTriggerAfterEarlierLifeLossBeforeEntry() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A later copy cannot trigger on a subsequent life loss in the same turn")
    void laterCopyDoesNotTriggerOnSecondLifeLoss() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Each copy present for the first life loss gives one energy")
    void bothCopiesTriggerOnFirstLifeLoss() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-damage life loss triggers and activation costs are paid before resolution")
    void triggersOnNonDamageLifeLossAndPaysCostsImmediately() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.addToBattlefield(player2, new GontisMachinations());
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isZero();
        harness.assertNotOnBattlefield(player2, "Gonti's Machinations");
        harness.assertInGraveyard(player2, "Gonti's Machinations");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent life loss does not award energy")
    void doesNotTriggerForOpponentLifeLoss() {
        harness.addToBattlefield(player1, new GontisMachinations());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }
}
