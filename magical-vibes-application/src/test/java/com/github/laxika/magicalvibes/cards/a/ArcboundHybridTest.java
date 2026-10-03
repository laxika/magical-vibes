package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundHybrid.class, DrossGolem.class, DroolingOgre.class, EchoingDecay.class})
class ArcboundHybridTest extends BaseCardTest {

    @Test
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.castFromHand(player1, new ArcboundHybrid(), "{4}");
        harness.passBothPriorities();

        Permanent hybrid = findPermanent(player1, "Arcbound Hybrid");
        assertThat(hybrid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent drossGolem = addCreatureReady(player1, new DrossGolem());
        Permanent opponentDrossGolem = addCreatureReady(player2, new DrossGolem());
        Permanent ogre = addCreatureReady(player1, new DroolingOgre());

        destroyHybrid(hybrid);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(drossGolem.getId(), opponentDrossGolem.getId())
                .doesNotContain(ogre.getId());

        harness.handlePermanentChosen(player1, drossGolem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(drossGolem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularMayDeclineToPutItsCountersOnTargetArtifactCreature() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent drossGolem = addCreatureReady(player1, new DrossGolem());

        destroyHybrid(hybrid);

        harness.handlePermanentChosen(player1, drossGolem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(drossGolem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canAttackOnTheTurnItEnters() {
        harness.castFromHand(player1, new ArcboundHybrid(), "{4}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat(player1);

        harness.assertLife(player2, 18);
    }

    @Test
    void modularUsesTheActualCounterCountAndAddsToExistingCounters() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent golem = addCreatureReady(player1, new DrossGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyHybrid(hybrid);
        harness.handlePermanentChosen(player1, golem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularCanGiveCountersToAnOpponentsArtifactCreature() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent golem = addCreatureReady(player2, new DrossGolem());

        destroyHybrid(hybrid);
        harness.handlePermanentChosen(player1, golem.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void modularDoesNothingWhenNoArtifactCreatureRemains() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent ogre = addCreatureReady(player1, new DroolingOgre());

        destroyHybrid(hybrid);

        harness.assertInGraveyard(player1, "Arcbound Hybrid");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularChoiceDescribesPlusOnePlusOneCounters() {
        Permanent hybrid = addCreatureReady(player1, new ArcboundHybrid());
        hybrid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent golem = addCreatureReady(player1, new DrossGolem());

        destroyHybrid(hybrid);
        harness.handlePermanentChosen(player1, golem.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("+1/+1").doesNotContain("-1/-1");
        harness.handleMayAbilityChosen(player1, false);
    }

    private void destroyHybrid(Permanent hybrid) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new EchoingDecay()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, hybrid.getId());
    }
}
