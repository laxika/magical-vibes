package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Banshee.class, Squire.class})
class BansheeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the rounded-down half to the target and rounded-up half to its controller")
    void dealsRoundedHalvesToTargetPlayerAndController() {
        addCreatureReady(player1, new Banshee());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 17);
    }

    @Test
    void roundsTargetHalfDownToZeroWhenXIsOne() {
        addCreatureReady(player1, new Banshee());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals the rounded-down half to a creature")
    void dealsRoundedDownHalfToCreature() {
        addCreatureReady(player1, new Banshee());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Squire());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 5, harness.getPermanentId(player2, "Squire"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Squire");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not deal controller damage when the target becomes illegal")
    void fizzlesWithoutControllerDamageWhenTargetLeaves() {
        addCreatureReady(player1, new Banshee());
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Squire());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, 5, harness.getPermanentId(player2, "Squire"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
