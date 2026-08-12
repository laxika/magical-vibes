package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcboundOverseerTest extends BaseCardTest {

    @Test
    void entersWithSixPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new ArcboundOverseer()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent overseer = findPermanent(player1, "Arcbound Overseer");
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void upkeepPutsCountersOnEachControlledCreatureWithModular() {
        Permanent overseer = addCreatureReady(player1, new ArcboundOverseer());
        overseer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent bruiser = addCreatureReady(player1, new ArcboundBruiser());
        bruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBruiser = addCreatureReady(player2, new ArcboundBruiser());
        opponentBruiser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(bruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayPutItsCountersOnTargetArtifactCreatureWhenItDies() {
        Permanent overseer = addCreatureReady(player1, new ArcboundOverseer());
        overseer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);
        overseer.tap();
        Permanent bronzeSable = addCreatureReady(player1, new BronzeSable());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        destroyOverseer(overseer);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bronzeSable.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, bronzeSable.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bronzeSable.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    private void destroyOverseer(Permanent overseer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, overseer.getId(), null);
        harness.passBothPriorities();
    }
}
