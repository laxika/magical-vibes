package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.u.UndiscoveredParadise;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JujuBubble.class, Impulse.class, UndiscoveredParadise.class})
class JujuBubbleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell sacrifices Juju Bubble")
    void castingSpellSacrifices() {
        harness.addToBattlefield(player1, new JujuBubble());
        harness.castFromHand(player1, new Impulse(), "{1}{U}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Juju Bubble");
        harness.assertInGraveyard(player1, "Juju Bubble");
    }

    @Test
    @DisplayName("Playing a land sacrifices Juju Bubble")
    void playingLandSacrifices() {
        harness.addToBattlefield(player1, new JujuBubble());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new UndiscoveredParadise()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Juju Bubble");
        harness.assertInGraveyard(player1, "Juju Bubble");
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Putting a land onto the battlefield without playing it does not sacrifice")
    void landEnteringWithoutPlayDoesNotSacrifice() {
        harness.addToBattlefield(player1, new JujuBubble());
        harness.enterBattlefieldAndReturn(player1, new UndiscoveredParadise());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Juju Bubble");
    }

    @Test
    @DisplayName("Opponent casting a spell does not sacrifice Juju Bubble")
    void opponentSpellDoesNotSacrifice() {
        harness.addToBattlefield(player1, new JujuBubble());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Impulse(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertOnBattlefield(player1, "Juju Bubble");
    }

    @Test
    @DisplayName("{2}: You gain 1 life")
    void activatedAbilityGainsLife() {
        harness.addToBattlefield(player1, new JujuBubble());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertOnBattlefield(player1, "Juju Bubble");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Juju Bubble")
    void decliningCumulativeUpkeepSacrifices() {
        harness.addToBattlefield(player1, new JujuBubble());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Juju Bubble");
        harness.assertInGraveyard(player1, "Juju Bubble");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Juju Bubble")
    void payingCumulativeUpkeepKeeps() {
        var bubble = harness.addToBattlefieldAndReturn(player1, new JujuBubble());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThat(bubble.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bubble);
    }

    @Test
    @DisplayName("The second cumulative upkeep costs {2}")
    void secondCumulativeUpkeepCostsTwo() {
        var bubble = harness.addToBattlefieldAndReturn(player1, new JujuBubble());
        bubble.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(bubble.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bubble);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
