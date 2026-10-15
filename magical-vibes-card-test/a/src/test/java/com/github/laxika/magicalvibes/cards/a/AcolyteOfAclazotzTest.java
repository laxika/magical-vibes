package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("The sacrifice and tap costs are paid before the life changes resolve")
    void paysCostsBeforeResolution() {
        addReadyAcolyte();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A summoning-sick Acolyte cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new AcolyteOfAclazotz()).setSummoningSick(true);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Acolyte cannot activate its tap ability")
    void cannotActivateWhileTapped() {
        addReadyAcolyte();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addToBattlefield(player1, new Millstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Millstone");
    }

    @Test
    @DisplayName("Opponent-controlled creatures and artifacts cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanents() {
        addReadyAcolyte();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Millstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Millstone");
    }

    private void addReadyAcolyte() {
        var acolyte = harness.addToBattlefieldAndReturn(player1, new AcolyteOfAclazotz());
        acolyte.setSummoningSick(false);
    }
}
