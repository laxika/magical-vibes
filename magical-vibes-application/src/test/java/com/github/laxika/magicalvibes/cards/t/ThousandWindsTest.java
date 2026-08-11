package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThousandWindsTest extends BaseCardTest {

    @Test
    void morphingReturnsOtherTappedCreaturesToTheirOwnersHands() {
        Permanent tappedOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedOwn.tap();
        Permanent untappedOwn = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent tappedOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOpponent.tap();
        Permanent untappedOpponent = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Island());
        tappedLand.tap();
        harness.setHand(player1, List.of(new ThousandWinds()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent thousandWinds = findPermanent(player1, "Thousand Winds");
        thousandWinds.tap();
        assertThat(thousandWinds.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thousandWinds));
        harness.passBothPriorities();

        assertThat(thousandWinds.isFaceDown()).isFalse();
        assertThat(thousandWinds.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
