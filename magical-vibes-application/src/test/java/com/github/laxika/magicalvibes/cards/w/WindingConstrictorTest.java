package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AetherPoisoner;
import com.github.laxika.magicalvibes.cards.m.MerenOfClanNelToth;
import com.github.laxika.magicalvibes.cards.n.NuclearFallout;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.p.PyramidOfThePantheon;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindingConstrictor.class, GrizzlyBears.class, IchorRats.class,
        PyramidOfThePantheon.class, TimberlandGuide.class, WalkingBallista.class,
        AetherPoisoner.class, NuclearFallout.class, MerenOfClanNelToth.class})
class WindingConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("adds one of each counter put on an artifact or creature you control")
    void addsCountersToArtifactsAndCreatures() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
    }

    @Test
    @DisplayName("adds one poison counter when you would get poison counters")
    void addsPoisonCounterToController() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void artifactCreatureEnteringGetsOnlyOneExtraCounterPerConstrictor() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 2);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Walking Ballista")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void zeroCountersAreNotIncreased() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new WalkingBallista()));

        harness.castArtifact(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Walking Ballista");
        harness.assertInGraveyard(player1, "Walking Ballista");
    }

    @Test
    void opposingConstrictorDoesNotIncreaseEntryCounters() {
        harness.addToBattlefield(player2, new WindingConstrictor());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 2);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Walking Ballista")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void energyEventGetsOneExtraCounterPerConstrictor() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.addToBattlefield(player2, new WindingConstrictor());
        harness.setHand(player1, List.of(new AetherPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void radCountersAreIncreasedOnlyForController() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void experienceCountersAreIncreased() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setHand(player1, List.of(new WalkingBallista()));

        harness.castArtifact(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }
}
