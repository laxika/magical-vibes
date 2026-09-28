package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbflareGremlin.class, Forest.class})
class BarbflareGremlinTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Barbflare Gremlin doubles land mana and deals damage")
    void tappedGremlinTriggersForControllerLand() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An untapped Barbflare Gremlin does not trigger")
    void untappedGremlinDoesNotTrigger() {
        addCreatureReady(player1, new BarbflareGremlin());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A tapped Barbflare Gremlin triggers for an opponent's land")
    void tappedGremlinTriggersForOpponentLand() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
