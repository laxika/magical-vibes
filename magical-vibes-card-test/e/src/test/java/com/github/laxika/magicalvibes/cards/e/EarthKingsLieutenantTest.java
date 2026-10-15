package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriors;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthKingsLieutenant.class, KyoshiWarriors.class, GrizzlyBears.class, Xenograft.class})
class EarthKingsLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on each other Ally creature when it enters")
    void putsCounterOnEachOtherAllyWhenEntering() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarthKingsLieutenant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent lieutenant = findPermanent(player1, "Earth King's Lieutenant");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a counter on itself for each other Ally that enters")
    void putsCounterOnItselfForEachOtherAllyEntering() {
        Permanent lieutenant = addCreatureReady(player1, new EarthKingsLieutenant());
        harness.setHand(player1, List.of(new KyoshiWarriors()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second Lieutenant gives the first two counters but does not counter itself")
    void secondLieutenantTriggersBothAbilitiesOnTheFirst() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EarthKingsLieutenant());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new KyoshiWarriors());
        harness.setHand(player1, List.of(new EarthKingsLieutenant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EarthKingsLieutenant)
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opposing Allies and their tokens do not trigger the Lieutenant")
    void opposingAlliesDoNotTriggerLieutenant() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new EarthKingsLieutenant());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new KyoshiWarriors()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An entering creature made an Ally by Xenograft triggers the Lieutenant")
    void enteringCreatureWithGrantedAllySubtypeTriggersLieutenant() {
        Permanent lieutenant = harness.addToBattlefieldAndReturn(player1, new EarthKingsLieutenant());
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.ALLY);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
