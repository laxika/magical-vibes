package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCrystalsChosen.class, GrizzlyBears.class, Plains.class})
class TheCrystalsChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four Heroes and puts a +1/+1 counter on each own creature")
    void createsHeroesAndCountersOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new TheCrystalsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();

        List<Permanent> heroes = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HERO))
                .toList();

        assertThat(heroes).hasSize(4);
        assertThat(heroes).allSatisfy(hero -> {
            assertThat(hero.getEffectivePower()).isEqualTo(2);
            assertThat(hero.getEffectiveToughness()).isEqualTo(2);
            assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolves without existing creatures and gives each new Hero a counter")
    void createsHeroesOnEmptyBattlefield() {
        harness.castFromHand(player1, new TheCrystalsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4).allSatisfy(hero -> {
            assertThat(hero.getCard().isToken()).isTrue();
            assertThat(hero.getCard().getSubtypes()).containsExactly(CardSubtype.HERO);
            assertThat(hero.getCard().getColors()).isEmpty();
            assertThat(hero.isTapped()).isFalse();
            assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(hero.getEffectivePower()).isEqualTo(2);
            assertThat(hero.getEffectiveToughness()).isEqualTo(2);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not put counters on noncreature permanents")
    void excludesNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new TheCrystalsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != land)).allSatisfy(hero ->
                assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }
}
