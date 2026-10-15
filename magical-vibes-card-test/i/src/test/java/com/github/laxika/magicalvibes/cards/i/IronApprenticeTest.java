package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.k.KamisFlare;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronApprentice.class, JukaiTrainee.class, KamisFlare.class})
class IronApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent apprentice = castIronApprentice();

        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, moves its counters to a creature you control")
    void deathTriggerMovesCountersToControlledCreature() {
        Permanent apprentice = castIronApprentice();
        Permanent bears = addCreatureReady(player1, new JukaiTrainee());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new KamisFlare()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, apprentice.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The death trigger cannot target an opponent's creature")
    void deathTriggerTargetsOnlyControlledCreatures() {
        Permanent apprentice = castIronApprentice();
        Permanent bears = addCreatureReady(player1, new JukaiTrainee());
        Permanent opponentBears = addCreatureReady(player2, new JukaiTrainee());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new KamisFlare()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, apprentice.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(opponentBears.getId());
    }

    @Test
    @DisplayName("Transfers the full quantity of every counter type")
    void transfersAllCounterTypesAndQuantities() {
        Permanent apprentice = castIronApprentice();
        apprentice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        apprentice.setCounterCount(CounterType.CHARGE, 3);
        apprentice.setCounterCount(CounterType.FLYING, 1);
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new KamisFlare()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, apprentice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Iron Apprentice");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when it dies without counters")
    void noDeathTriggerWithoutCounters() {
        Permanent apprentice = castIronApprentice();
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        apprentice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Iron Apprentice");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DeathTriggerTarget.class)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not put counters on a creature that leaves before resolution")
    void targetLeavingBeforeResolutionDoesNotReceiveCounters() {
        Permanent apprentice = castIronApprentice();
        Permanent target = addCreatureReady(player1, new JukaiTrainee());
        Permanent otherCreature = addCreatureReady(player1, new JukaiTrainee());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new KamisFlare(), new KamisFlare()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, apprentice.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jukai Trainee");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent castIronApprentice() {
        harness.setHand(player1, List.of(new IronApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Iron Apprentice");
    }
}
