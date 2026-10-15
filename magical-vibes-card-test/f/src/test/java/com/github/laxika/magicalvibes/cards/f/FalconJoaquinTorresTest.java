package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconJoaquinTorres.class, GrizzlyBears.class, Forest.class})
class FalconJoaquinTorresTest extends BaseCardTest {

    @Test
    void battalionPutsCounterAndScries() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNotNull()
                .extracting(PendingInteraction.Scry::cards)
                .asList()
                .hasSize(1);
    }

    @Test
    void battalionDoesNotTriggerWithoutTwoOtherAttackers() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void battalionCanPutTheTopCardOnTheBottom() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
    }

    @Test
    void battalionCanKeepTheTopCard() {
        addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
    }

    @Test
    void battalionStillPutsCounterWithAnEmptyLibrary() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void battalionRequiresFalconToAttack() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void battalionStillResolvesAfterAnotherAttackerLeaves() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1, 2)));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other);
        resolveAllTriggers();

        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
    }

    @Test
    void battalionStillScriesAfterFalconLeaves() {
        Permanent falcon = addCreatureReady(player1, new FalconJoaquinTorres());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0, 1, 2)));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, falcon);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
