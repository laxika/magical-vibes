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
        harness.setHand(player1, List.of(new ArcboundHybrid()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
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

    private void destroyHybrid(Permanent hybrid) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new EchoingDecay()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, hybrid.getId());
    }
}
