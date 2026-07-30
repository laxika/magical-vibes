package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SwitcherooTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Switcheroo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of the two target creatures")
    void exchangesControl() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchanges control when the opponent's creature is the first target")
    void exchangesControlWithOpponentCreatureFirst() {
        prepare();
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(opponents.getId(), own.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when both target creatures have the same controller (CR 701.12b)")
    void doesNothingWhenSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchange fizzles when a target creature leaves the battlefield before resolution (CR 701.12a)")
    void fizzlesWhenTargetGone() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponents);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
