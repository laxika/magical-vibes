package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BondersOrnament.class, GrizzlyBears.class})
class BondersOrnamentTest extends BaseCardTest {

    @Test
    void tapsForOneManaOfAnyColor() {
        Permanent ornament = harness.addToBattlefieldAndReturn(player1, new BondersOrnament());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(ornament.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachPlayerWithAnOrnamentDraws() {
        Permanent player1Ornament = harness.addToBattlefieldAndReturn(player1, new BondersOrnament());
        harness.addToBattlefield(player2, new BondersOrnament());
        Card player1Draw = new GrizzlyBears();
        Card player2Draw = new GrizzlyBears();
        setDeck(player1, List.of(player1Draw));
        setDeck(player2, List.of(player2Draw));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(player1Ornament), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Draw);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(player1Ornament.isTapped()).isTrue();
    }

    @Test
    void playersWithoutAnOrnamentDoNotDraw() {
        Permanent ornament = harness.addToBattlefieldAndReturn(player1, new BondersOrnament());
        Card player1Draw = new GrizzlyBears();
        Card player2Card = new GrizzlyBears();
        setDeck(player1, List.of(player1Draw));
        setDeck(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player1, indexOf(ornament), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
