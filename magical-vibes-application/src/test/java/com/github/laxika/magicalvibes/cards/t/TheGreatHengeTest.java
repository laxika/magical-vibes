package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.s.ScorchingDragonfire;
import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatHenge.class, GrizzlyBears.class, ZombieInfestation.class,
        MarchOfTheMachines.class, ScorchingDragonfire.class})
class TheGreatHengeTest extends BaseCardTest {

    @Test
    @DisplayName("Costs less by the greatest power among creatures you control")
    void reducesCostByGreatestControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheGreatHenge()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapping adds two green mana and gains two life")
    void tappingAddsManaAndLife() {
        Permanent henge = harness.addToBattlefieldAndReturn(player1, new TheGreatHenge());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        assertThat(henge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nontoken creature entering under your control gets a counter and draws a card")
    void nontokenCreatureEnteringGetsCounterAndDraws() {
        harness.addToBattlefield(player1, new TheGreatHenge());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent enteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A token entering under your control does not trigger the ability")
    void tokenEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheGreatHenge());
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void paysFullCostWithoutControlledCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheGreatHenge()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void usesGreatestEffectivePowerRatherThanSum() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        larger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new TheGreatHenge()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessPowerDoesNotReduceColoredManaCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        harness.setHand(player1, List.of(new TheGreatHenge()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheGreatHenge());
        harness.setHand(player1, List.of());

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsEvenWhenEnteringCreatureLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new TheGreatHenge());
        harness.setHand(player1, List.of(new ScorchingDragonfire()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void triggersForItsOwnEntryWhenMarchOfTheMachinesMakesItACreature() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setHand(player1, List.of(new TheGreatHenge()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent henge = findPermanent(player1, "The Great Henge");
        assertThat(henge.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
