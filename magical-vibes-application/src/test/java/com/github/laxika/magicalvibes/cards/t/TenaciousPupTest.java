package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenaciousPup.class, GrizzlyBears.class})
class TenaciousPupTest extends BaseCardTest {

    @Test
    void gainsLifeAndEmpowersTheNextCreatureSpellOnce() {
        TenaciousPup pup = new TenaciousPup();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(pup, firstBears));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent empowered = findPermanentByCardId(firstBears.getId());
        assertThat(empowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(empowered.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, empowered, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, empowered, Keyword.VIGILANCE)).isTrue();

        harness.setHand(player1, List.of(secondBears));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent unempowered = findPermanentByCardId(secondBears.getId());
        assertThat(unempowered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(unempowered.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
