package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoubleVision;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.cards.s.SculptorOfWinter;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlessingOfFrost.class, SculptorOfWinter.class, RavenousLindwurm.class, DoubleVision.class,
        SnowCoveredForest.class, com.github.laxika.magicalvibes.cards.f.Forest.class})
class BlessingOfFrostTest extends BaseCardTest {

    @Test
    void distributesSnowCountersAmongControlledCreaturesAndDrawsForPowerFourCreatures() {
        addSnowManaSources(2);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.minValue()).isZero();
        assertThat(firstChoice.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.XValueChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.minValue()).isZero();
        assertThat(secondChoice.maxValue()).isEqualTo(1);
        harness.handleXValueChosen(player1, 0);

        PendingInteraction.XValueChoice finalChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(finalChoice).isNotNull();
        assertThat(finalChoice.minValue()).isEqualTo(1);
        assertThat(finalChoice.maxValue()).isEqualTo(1);
        harness.handleXValueChosen(player1, 1);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void countersPlacedBeforePowerThresholdDrawIsCounted() {
        addSnowManaSources(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void manaNotFromSnowSourcesDoesNotProvideCounters() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.tapPermanent(player1, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addToBattlefield(player1, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addSnowManaSources(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }
        for (int i = 0; i < count; i++) {
            harness.tapPermanent(player1, i);
        }
    }

    @Test
    void drawsForEachCreatureThatReachesPowerFourAfterDistribution() {
        addSnowManaSources(4);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addToBattlefield(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter(), new SculptorOfWinter(),
                new SculptorOfWinter()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player1, 2);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void assigningAllCountersToFirstCreatureSkipsRemainingCreatures() {
        addSnowManaSources(4);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotDrawWhenAllControlledCreaturesRemainBelowPowerFour() {
        addSnowManaSources(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SculptorOfWinter());
        harness.addToBattlefield(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutControlledCreaturesEvenWhenSnowManaWasSpent() {
        addSnowManaSources(4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blessing of Frost");
    }

    @Test
    void copyDrawsCardsButDoesNotInheritSnowManaSpentOnOriginal() {
        addSnowManaSources(4);
        harness.addToBattlefield(player1, new DoubleVision());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RavenousLindwurm());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new SculptorOfWinter(), new SculptorOfWinter()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 4);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
