package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DrossGolem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherSnap.class, DarksteelCitadel.class, DrossGolem.class})
class AetherSnapTest extends BaseCardTest {

    @Test
    @DisplayName("Removes every counter from every permanent")
    void removesAllCountersFromAllPermanents() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new DrossGolem());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownPermanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        ownPermanent.setCounterCount(CounterType.OIL, 4);
        opponentPermanent.setCounterCount(CounterType.CHARGE, 3);
        opponentPermanent.setCounterCount(CounterType.LORE, 1);
        castAetherSnap();

        assertThat(ownPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownPermanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(ownPermanent.getCounterCount(CounterType.OIL)).isZero();
        assertThat(opponentPermanent.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(opponentPermanent.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("Exiles tokens on both battlefields and leaves nontoken permanents")
    void exilesAllTokens() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new DrossGolem());
        harness.addToBattlefield(player1, token(new DrossGolem()));
        harness.addToBattlefield(player2, token(new DarksteelCitadel()));
        castAetherSnap();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentPermanent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(Card::isToken);
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(Card::isToken);
    }

    private void castAetherSnap() {
        harness.castFromHand(player1, new AetherSnap(), "{3}{B}{B}");
        harness.passBothPriorities();
    }

    private Card token(Card card) {
        card.setToken(true);
        return card;
    }
}
