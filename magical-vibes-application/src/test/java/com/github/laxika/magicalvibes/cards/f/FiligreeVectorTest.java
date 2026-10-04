package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiligreeVector.class, GrizzlyBears.class, Ornithopter.class, Spellbook.class})
class FiligreeVectorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters on target creatures and artifacts")
    void entersWithCountersOnTargetCreaturesAndArtifacts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, spellbook.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(spellbook.getCounterCount(CounterType.CHARGE)).isOne();
    }

    @Test
    @DisplayName("An artifact creature can be targeted in both ETB groups")
    void artifactCreatureCanBeTargetedInBothGroups() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(ornithopter.getCounterCount(CounterType.CHARGE)).isOne();
    }

    @Test
    @DisplayName("The activated ability sacrifices another artifact and proliferates")
    void activatedAbilitySacrificesAnotherArtifactAndProliferates() {
        Permanent vector = addCreatureReady(player1, new FiligreeVector());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(vector.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spellbook);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Filigree Vector itself")
    void activatedAbilityCannotSacrificeItself() {
        addCreatureReady(player1, new FiligreeVector());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Filigree Vector can target itself in both ETB groups")
    void canTargetItselfInBothGroups() {
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        Permanent vector = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, vector.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, vector.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(vector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(vector.getCounterCount(CounterType.CHARGE)).isOne();
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new FiligreeVector());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpposingArtifact() {
        addCreatureReady(player1, new FiligreeVector());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("Both ETB target groups may be empty")
    void canChooseNoEtbTargets() {
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent vector = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vector.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The ETB may target multiple opposing creatures while choosing no artifacts")
    void canTargetMultipleOpposingCreaturesOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(first.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(second.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The ETB may choose artifacts while choosing no creatures")
    void canTargetOpposingArtifactOnly() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isOne();
    }

    @Test
    @DisplayName("Proliferate adds every existing counter kind to chosen permanents and players")
    void proliferatesEveryExistingCounterKind() {
        addCreatureReady(player1, new FiligreeVector());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Choosing nothing for proliferate still pays the activation costs")
    void canDeclineAllProliferateChoices() {
        Permanent vector = addCreatureReady(player1, new FiligreeVector());
        harness.addToBattlefield(player1, new Spellbook());
        vector.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(vector.isTapped()).isTrue();
        assertThat(vector.getCounterCount(CounterType.CHARGE)).isOne();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Any number of creature targets includes one hundred creatures")
    void canTargetOneHundredCreatures() {
        List<Permanent> creatures = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()))
                .toList();
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        for (Permanent creature : creatures) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne());
    }

    @Test
    @DisplayName("Any number of artifact targets includes one hundred artifacts")
    void canTargetOneHundredArtifacts() {
        List<Permanent> artifacts = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Spellbook()))
                .toList();
        harness.castFromHand(player1, new FiligreeVector(), "{3}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        for (Permanent artifact : artifacts) {
            harness.handlePermanentChosen(player1, artifact.getId());
        }
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(artifacts).allSatisfy(artifact ->
                assertThat(artifact.getCounterCount(CounterType.CHARGE)).isOne());
    }
}
