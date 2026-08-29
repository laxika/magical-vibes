package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcolyteOfAclazotz.class, GrizzlyBears.class, Millstone.class})
class AcolyteOfAclazotzTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature makes each opponent lose 1 life and its controller gain 1 life")
    void sacrificesCreatureAndDrainsOpponent() {
        addReadyAcolyte();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Acolyte of Aclazotz");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another artifact makes each opponent lose 1 life and its controller gain 1 life")
    void sacrificesArtifactAndDrainsOpponent() {
        addReadyAcolyte();
        harness.addToBattlefield(player1, new Millstone());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("The ability cannot sacrifice Acolyte of Aclazotz itself")
    void cannotSacrificeItself() {
        addReadyAcolyte();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyAcolyte() {
        var acolyte = harness.addToBattlefieldAndReturn(player1, new AcolyteOfAclazotz());
        acolyte.setSummoningSick(false);
    }
}
