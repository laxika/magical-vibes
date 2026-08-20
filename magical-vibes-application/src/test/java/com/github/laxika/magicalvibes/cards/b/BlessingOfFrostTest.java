package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlessingOfFrostTest extends BaseCardTest {

    @Test
    void distributesSnowCountersAmongControlledCreaturesAndDrawsForPowerFourCreatures() {
        addSnowManaSources(2);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new BlessingOfFrost()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
}
