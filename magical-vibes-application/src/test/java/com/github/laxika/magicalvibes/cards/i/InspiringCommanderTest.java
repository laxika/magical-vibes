package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringCommander.class, GrizzlyBears.class, HillGiant.class,
        GloriousAnthem.class, SanctuaryCat.class})
class InspiringCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life and draws a card when a creature with power 2 or less enters")
    void gainsLifeAndDrawsForSmallCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power greater than 2")
    void doesNotTriggerForLargeCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when Inspiring Commander enters")
    void doesNotTriggerForItself() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new InspiringCommander(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a printed 2-power creature entering as a 3-power creature")
    void doesNotTriggerWhenContinuousBoostRaisesEnteringPowerAboveTwo() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers for a creature with power less than 2")
    void triggersForOnePowerCreature() {
        harness.setLibrary(player1, List.of(new InspiringCommander()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());

        harness.enterBattlefieldAndReturn(player1, new SanctuaryCat());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Inspiring Commander");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The trigger still resolves if the entering creature's power increases")
    void doesNotRecheckPowerAtResolution() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());

        var enteringCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gd.stack).hasSize(1);
        enteringCreature.setPowerModifier(3);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An existing Commander triggers for another Commander entering")
    void triggersForAnotherCommander() {
        harness.setLibrary(player1, List.of(new InspiringCommander()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new InspiringCommander());

        harness.enterBattlefieldAndReturn(player1, new InspiringCommander());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
