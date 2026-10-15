package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({StarPupil.class, GrizzlyBears.class, Shock.class})
class StarPupilTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent pupil = castStarPupil();

        assertThat(pupil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, moves all counters to a creature you control")
    void deathTriggerMovesCountersToControlledCreature() {
        Permanent pupil = castStarPupil();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        pupil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, pupil.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The death trigger cannot target an opponent's creature")
    void deathTriggerTargetsOnlyControlledCreatures() {
        Permanent pupil = castStarPupil();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, pupil.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(harness.getPermanentId(player1, "Grizzly Bears"));
    }

    @Test
    @DisplayName("Dying without counters still requires a target")
    void deathWithoutCountersStillTriggers() {
        Permanent pupil = castStarPupil();
        Permanent recipient = addCreatureReady(player1, new StarPupil());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        pupil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Star Pupil");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transfers every kind of counter and adds to existing counters")
    void deathTransfersAllCounterTypes() {
        Permanent pupil = castStarPupil();
        Permanent recipient = addCreatureReady(player1, new StarPupil());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        recipient.setCounterCount(CounterType.CHARGE, 2);
        pupil.setCounterCount(CounterType.CHARGE, 3);
        pupil.setCounterCount(CounterType.FLYING, 1);
        pupil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Star Pupil");
    }

    @Test
    @DisplayName("Dying with no legal target does not transfer counters to the opponent")
    void noControlledCreatureMeansNoTransfer() {
        Permanent pupil = castStarPupil();
        Permanent opponent = addCreatureReady(player2, new StarPupil());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        pupil.setCounterCount(CounterType.CHARGE, 2);
        pupil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Star Pupil");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.CHARGE)).isZero();
    }
    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent castStarPupil() {
        harness.setHand(player1, List.of(new StarPupil()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Star Pupil");
    }
}
