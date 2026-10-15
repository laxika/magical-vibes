package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JwarIsleRefuge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroicFeast.class, GrizzlyBears.class, JwarIsleRefuge.class, BeaconOfImmortality.class})
class HeroicFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        harness.setHand(player1, List.of(new HeroicFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
    }

    @Test
    @DisplayName("Puts counters on up to as many creatures as life gained")
    void putsCountersOnUpToLifeGainedCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroicFeast());

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new JwarIsleRefuge()));
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void foodCanBeSacrificedImmediatelyToGainThreeLifeAndCounterThreeCreatures() {
        harness.setHand(player1, List.of(new HeroicFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        List<Permanent> creatures = IntStream.range(0, 4)
                .mapToObj(i -> addCreatureReady(player1, new GrizzlyBears())).toList();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertLife(player1, 23);
        for (int i = 0; i < 3; i++) {
            harness.handlePermanentChosen(player1, creatures.get(i).getId());
        }
        resolveAllTriggers();

        assertThat(creatures.subList(0, 3)).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(creatures.get(3).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayChooseNoTargetsDespiteHavingCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player1, 3);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayStopAfterFewerTargetsThanLifeGained() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player1, 3);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetsMustBeDistinctCreaturesYouControl() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player1, 3);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                findPermanent(player1, "Heroic Feast").getId())).isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, first.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentLifeGainDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player2, 3);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void lifeGainWithNoCreaturesResolvesWithoutTargets() {
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player1, 3);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canTargetMoreThanOneHundredCreaturesWhenThatMuchLifeIsGained() {
        List<Permanent> creatures = IntStream.range(0, 101)
                .mapToObj(i -> addCreatureReady(player1, new GrizzlyBears())).toList();
        harness.addToBattlefield(player1, new HeroicFeast());

        gainLifeWithBeacon(player1, 101);
        for (Permanent creature : creatures) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        resolveAllTriggers();

        assertThat(creatures).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    private void gainLifeWithBeacon(Player recipient, int amount) {
        harness.setLife(recipient, amount);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0, recipient.getId());
        harness.assertLife(recipient, amount * 2);
    }
}
