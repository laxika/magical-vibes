package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NissaVoiceOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfChandra.class, GideonOfTheTrials.class, GrizzlyBears.class, NissaVoiceOfZendikar.class})
class OathOfChandraTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, deals 3 damage to target creature an opponent controls")
    void entersAndDealsDamageToOpponentCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOathOfChandra();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enter-the-battlefield ability only offers creatures an opponent controls")
    void etbOnlyTargetsOpponentCreatures() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOathOfChandra();

        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("At each end step, deals 2 damage to each opponent after a planeswalker enters under its controller's control")
    void dealsDamageAtEndStepAfterPlaneswalkerEnters() {
        harness.addToBattlefield(player1, new OathOfChandra());
        castGideonOfTheTrials(player1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger at end step without a planeswalker entering under its controller's control")
    void doesNotTriggerWithoutControlledPlaneswalkerEntry() {
        harness.addToBattlefield(player1, new OathOfChandra());
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's planeswalker entry does not enable the end-step ability")
    void opponentPlaneswalkerEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OathOfChandra());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new NissaVoiceOfZendikar(), "{1}{G}{G}");
        harness.passBothPriorities();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A planeswalker entering before Oath still enables its end-step ability")
    void planeswalkerCanEnterBeforeOath() {
        harness.castFromHand(player1, new NissaVoiceOfZendikar(), "{1}{G}{G}");
        harness.passBothPriorities();
        castOathOfChandra();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oath of Chandra");
        assertThat(gd.stack).isEmpty();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A planeswalker already on the battlefield does not count as entering this turn")
    void existingPlaneswalkerDoesNotTrigger() {
        harness.addToBattlefield(player1, new OathOfChandra());
        harness.addToBattlefield(player1, new NissaVoiceOfZendikar());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple planeswalker entries deal only 2 damage even after those planeswalkers leave")
    void multipleDepartedPlaneswalkersStillDealOnlyTwoDamage() {
        harness.addToBattlefield(player1, new OathOfChandra());
        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new NissaVoiceOfZendikar(), "{1}{G}{G}");
            harness.passBothPriorities();
            var nissa = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().getName().equals("Nissa, Voice of Zendikar"))
                    .findFirst().orElseThrow();
            nissa.setCounterCount(CounterType.LOYALTY, 0);
            harness.runStateBasedActions();
        }
        harness.assertNotOnBattlefield(player1, "Nissa, Voice of Zendikar");

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void castOathOfChandra() {
        harness.castFromHand(player1, new OathOfChandra(), "{1}{R}");
    }

    private void castGideonOfTheTrials(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new GideonOfTheTrials(), "{1}{W}{W}");
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
