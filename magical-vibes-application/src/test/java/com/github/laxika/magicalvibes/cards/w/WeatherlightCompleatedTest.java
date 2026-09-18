package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeatherlightCompleated.class, GrizzlyBears.class, Forest.class, Murder.class})
class WeatherlightCompleatedTest extends BaseCardTest {

    @Test
    void becomesPhyrexianCreatureAtFourPhyresisCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());

        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
        assertThat(gqs.isArtifact(gd, weatherlight)).isTrue();

        weatherlight.setCounterCount(CounterType.PHYRESIS, 4);

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(gqs.isArtifact(gd, weatherlight)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, weatherlight, CardSubtype.PHYREXIAN)).isTrue();

        weatherlight.setCounterCount(CounterType.PHYRESIS, 3);

        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
    }

    @Test
    void putsCounterAndScriesBelowSevenPhyresisCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithMurder(creature);

        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void putsCounterAndDrawsAtSevenPhyresisCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithMurder(creature);

        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    private void destroyPlayerOneCreatureWithMurder(Permanent creature) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        gs.playCard(gd, player2, 0, 0, creature.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
