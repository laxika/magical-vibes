package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanShiTongLibrarian.class, DiabolicTutor.class, GrizzlyBears.class})
class WanShiTongLibrarianTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, puts X counters on itself and draws half X rounded down")
    void entersWithCountersAndDrawsHalfX() {
        WanShiTongLibrarian wanCard = new WanShiTongLibrarian();
        harness.setHand(player1, List.of(wanCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wan = permanentFor(wanCard);
        assertThat(wan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When an opponent searches their library, puts a counter on itself and draws a card")
    void triggersWhenOpponentSearchesLibrary() {
        Permanent wan = harness.addToBattlefieldAndReturn(player2, new WanShiTongLibrarian());
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(wan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent permanentFor(WanShiTongLibrarian card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
