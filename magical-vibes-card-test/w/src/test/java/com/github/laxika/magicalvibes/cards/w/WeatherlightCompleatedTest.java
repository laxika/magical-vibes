package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SunbathingRootwalla;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExtinguishTheLight;
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

@CardUsed({WeatherlightCompleated.class, SunbathingRootwalla.class, Forest.class, ExtinguishTheLight.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithExtinguishTheLight(creature);

        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void putsCounterAndDrawsAtSevenPhyresisCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithExtinguishTheLight(creature);

        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    void scriesWhenItDiesAsACreatureBelowSevenCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithExtinguishTheLight(weatherlight);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(weatherlight.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void drawsWhenItDiesWithSevenCounters() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        destroyPlayerOneCreatureWithExtinguishTheLight(weatherlight);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(weatherlight.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SunbathingRootwalla());
        harness.setHand(player1, List.of(new ExtinguishTheLight()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void checksCounterThresholdAtResolution() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 5);
        resolveAllTriggers();

        assertThat(weatherlight.getCounterCount(CounterType.PHYRESIS)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void pendingTriggerUsesCountersAtDepartureWithoutAddingAnotherCounter() {
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new WeatherlightCompleated());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SunbathingRootwalla());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new ExtinguishTheLight(), new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 8);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        weatherlight.setCounterCount(CounterType.PHYRESIS, 7);
        harness.castAndResolveInstant(player2, 0, weatherlight.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyPlayerOneCreatureWithExtinguishTheLight(Permanent creature) {
        harness.setHand(player2, List.of(new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();
    }
}
