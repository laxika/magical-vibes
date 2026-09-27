package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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
}
