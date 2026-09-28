package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsOfTyr.class, GrizzlyBears.class})
class ChampionsOfTyrTest extends BaseCardTest {

    @Test
    void choosesPlusOneCounterForNextCreatureSpell() {
        GrizzlyBears bears = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(bears.getId());
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(permanent.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(permanent.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void choosesFlyingCounterForNextCreatureSpell() {
        GrizzlyBears bears = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a flying counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(bears.getId());
        assertThat(permanent.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void choosesLifelinkCounterForNextCreatureSpell() {
        GrizzlyBears bears = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a lifelink counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(bears.getId());
        assertThat(permanent.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void boonIsConsumedByOnlyOneCreatureSpell() {
        ChampionsOfTyr champions = new ChampionsOfTyr();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(champions, firstBears));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.setHand(player1, List.of(firstBears));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();

        harness.setHand(player1, List.of(secondBears));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(firstBears.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanent(secondBears.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private GrizzlyBears castChampionsThenCreature() {
        ChampionsOfTyr champions = new ChampionsOfTyr();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(champions, bears));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return bears;
    }

    private Permanent findPermanent(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
