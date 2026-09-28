package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RibbonsOfTheReikai.class, TeardropKami.class, NinjaOfTheDeepHours.class})
class RibbonsOfTheReikaiTest extends BaseCardTest {

    private void castRibbons() {
        harness.setLibrary(player1, List.of(
                new NinjaOfTheDeepHours(), new NinjaOfTheDeepHours(), new NinjaOfTheDeepHours(),
                new NinjaOfTheDeepHours(), new NinjaOfTheDeepHours(), new NinjaOfTheDeepHours()));
        harness.castFromHand(player1, new RibbonsOfTheReikai(), "{4}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws a card for each Spirit the caster controls")
    void drawsOnePerSpirit() {
        harness.addToBattlefield(player1, new TeardropKami());
        harness.addToBattlefield(player1, new TeardropKami());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws nothing when no Spirits are controlled")
    void drawsNothingWithoutSpirits() {
        harness.addToBattlefield(player1, new NinjaOfTheDeepHours());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Spirits controlled by the opponent are not counted")
    void ignoresOpponentSpirits() {
        harness.addToBattlefield(player1, new TeardropKami());
        harness.addToBattlefield(player2, new TeardropKami());
        harness.addToBattlefield(player2, new TeardropKami());

        castRibbons();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
