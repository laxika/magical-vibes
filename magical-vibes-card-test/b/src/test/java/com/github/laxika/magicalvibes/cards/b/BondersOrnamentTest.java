package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BondersOrnament.class})
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
        Card player1Draw = new BondersOrnament();
        Card player2Draw = new BondersOrnament();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));
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
        Card player1Draw = new BondersOrnament();
        Card player2Card = new BondersOrnament();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int player2HandBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player1, indexOf(ornament), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandBefore);
    }

    @Test
    void multipleOrnamentsStillDrawOnlyOneCardPerPlayer() {
        harness.addToBattlefield(player1, new BondersOrnament());
        harness.addToBattlefield(player1, new BondersOrnament());
        harness.addToBattlefield(player2, new BondersOrnament());
        harness.addToBattlefield(player2, new BondersOrnament());
        Card first = new BondersOrnament();
        Card second = new BondersOrnament();
        Card opponentFirst = new BondersOrnament();
        Card opponentSecond = new BondersOrnament();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentFirst, opponentSecond));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentFirst);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSecond);
    }

    @Test
    void sourceLeavingBeforeResolutionDoesNotStopOpponentDrawing() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BondersOrnament());
        harness.addToBattlefield(player2, new BondersOrnament());
        Card ownCard = new BondersOrnament();
        Card opponentCard = new BondersOrnament();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void opponentGainingAnOrnamentBeforeResolutionDraws() {
        harness.addToBattlefield(player1, new BondersOrnament());
        Card ownCard = new BondersOrnament();
        Card opponentCard = new BondersOrnament();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player2, new BondersOrnament());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

}
