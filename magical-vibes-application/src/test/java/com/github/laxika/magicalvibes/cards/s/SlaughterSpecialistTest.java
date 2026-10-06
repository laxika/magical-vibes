package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.b.BloodlineCulling;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlaughterSpecialist.class, Assassinate.class, InfernalGrasp.class, BloodlineCulling.class})
class SlaughterSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent creates a 1/1 white Human token when it enters")
    void eachOpponentCreatesHumanToken() {
        castSpecialist();

        assertThat(countPermanents(player1, "Human")).isZero();
        assertThat(countPermanents(player2, "Human")).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when an opponent's creature dies")
    void gainsCounterWhenOpponentCreatureDies() {
        Permanent specialist = castSpecialist();
        Permanent human = findPermanent(player2, "Human");
        human.tap();

        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, human.getId());
        resolveAllTriggers();

        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A friendly creature dying does not give a counter")
    void friendlyCreatureDeathDoesNotGiveCounter() {
        Permanent specialist = castSpecialist();
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new SlaughterSpecialist());

        harness.setHand(player1, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, friendly.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Slaughter Specialist");
        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each opposing creature dying simultaneously gives a separate counter")
    void simultaneousOpponentDeathsGiveSeparateCounters() {
        Permanent first = castSpecialist();
        Permanent second = castSpecialist();
        assertThat(countPermanents(player2, "Human")).isEqualTo(2);

        harness.setHand(player1, List.of(new BloodlineCulling()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 1, null);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Human")).isZero();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opposing nontoken creature dying also gives a counter")
    void opponentNontokenDeathGivesCounter() {
        Permanent specialist = castSpecialist();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SlaughterSpecialist());

        harness.setHand(player1, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Slaughter Specialist");
        assertThat(specialist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter trigger creates a Human even if the Specialist leaves first")
    void enterTriggerResolvesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new SlaughterSpecialist()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent specialist = findPermanent(player1, "Slaughter Specialist");
        assertThat(countPermanents(player2, "Human")).isZero();

        harness.setHand(player1, List.of(new InfernalGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, specialist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slaughter Specialist");
        assertThat(countPermanents(player2, "Human")).isEqualTo(1);
        assertThat(countPermanents(player1, "Human")).isZero();
    }

    private Permanent castSpecialist() {
        harness.setHand(player1, List.of(new SlaughterSpecialist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        List<Permanent> specialists = findPermanents(player1, "Slaughter Specialist");
        return specialists.get(specialists.size() - 1);
    }
}
