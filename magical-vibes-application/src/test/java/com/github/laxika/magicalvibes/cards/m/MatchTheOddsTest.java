package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LoyalFireSage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MatchTheOdds.class, LoyalFireSage.class})
class MatchTheOddsTest extends BaseCardTest {

    @Test
    void createsAnAllyWithCountersEqualToOpponentsCreatures() {
        harness.addToBattlefield(player1, new LoyalFireSage());
        harness.addToBattlefield(player2, new LoyalFireSage());
        harness.addToBattlefield(player2, new LoyalFireSage());

        castMatchTheOdds();

        Permanent ally = token();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ally.getEffectivePower()).isEqualTo(3);
        assertThat(ally.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void ownCreaturesDoNotIncreaseTheCounterCount() {
        harness.addToBattlefield(player1, new LoyalFireSage());
        harness.addToBattlefield(player1, new LoyalFireSage());

        castMatchTheOdds();

        Permanent ally = token();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getEffectivePower()).isEqualTo(1);
        assertThat(ally.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void createsExactlyOneTokenOnAnEmptyBattlefield() {
        castMatchTheOdds();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent ally = token();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getEffectivePower()).isEqualTo(1);
        assertThat(ally.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Match the Odds");
    }

    @Test
    void countsCreaturesAtResolutionRatherThanWhenCast() {
        harness.addToBattlefield(player2, new LoyalFireSage());
        harness.castFromHand(player1, new MatchTheOdds(), "{2}{G}");
        harness.addToBattlefield(player2, new LoyalFireSage());

        harness.passBothPriorities();

        assertThat(token().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void aSecondCastOnlyPutsCountersOnItsOwnToken() {
        harness.addToBattlefield(player2, new LoyalFireSage());
        castMatchTheOdds();
        Permanent firstAlly = token();
        harness.addToBattlefield(player2, new LoyalFireSage());

        castMatchTheOdds();

        assertThat(firstAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent secondAlly = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(firstAlly.getId()))
                .findFirst().orElseThrow();
        assertThat(secondAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castMatchTheOdds() {
        harness.castFromHand(player1, new MatchTheOdds(), "{2}{G}");
        harness.passBothPriorities();
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
