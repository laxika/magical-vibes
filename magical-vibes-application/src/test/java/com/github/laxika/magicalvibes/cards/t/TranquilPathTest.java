package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelfireCrusader;
import com.github.laxika.magicalvibes.cards.u.UnnaturalSelection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilPath.class, UnnaturalSelection.class, AngelfireCrusader.class})
class TranquilPathTest extends BaseCardTest {

    @Test
    void destroysAllEnchantmentsAndDrawsACard() {
        harness.addToBattlefield(player1, new UnnaturalSelection());
        harness.addToBattlefield(player2, new UnnaturalSelection());
        harness.addToBattlefield(player2, new AngelfireCrusader());
        harness.setLibrary(player1, List.of(new AngelfireCrusader()));

        harness.castFromHand(player1, new TranquilPath(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Angelfire Crusader");
        harness.assertInGraveyard(player1, "Unnatural Selection");
        harness.assertInGraveyard(player2, "Unnatural Selection");
        harness.assertInHand(player1, "Angelfire Crusader");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
