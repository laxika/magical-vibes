package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SellSwordBrute.class, Sewerdreg.class})
class SellSwordBruteTest extends BaseCardTest {

    @Test
    @DisplayName("When Sell-Sword Brute dies, it deals 2 damage to its controller")
    void deathTriggerDamagesController() {
        addCreatureReady(player1, new SellSwordBrute());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killSellSwordBrute();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Sell-Sword Brute");
    }

    @Test
    @DisplayName("When an opponent controls Sell-Sword Brute, its death trigger damages that opponent")
    void deathTriggerDamagesItsControllerWhenOpponentControlsIt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new Sewerdreg());
        Permanent brute = addCreatureReady(player2, new SellSwordBrute());
        resolveCombat(attacker, brute);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Sell-Sword Brute");
    }

    @Test
    @DisplayName("Brutes dying in the same combat each damage their own controller")
    void simultaneousDeathsDamageBothControllers() {
        Permanent attacker = addCreatureReady(player1, new SellSwordBrute());
        Permanent blocker = addCreatureReady(player2, new SellSwordBrute());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat(attacker, blocker);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Sell-Sword Brute");
        harness.assertInGraveyard(player2, "Sell-Sword Brute");
        harness.assertNotOnBattlefield(player1, "Sell-Sword Brute");
        harness.assertNotOnBattlefield(player2, "Sell-Sword Brute");
    }

    private void killSellSwordBrute() {
        Permanent brute = findPermanent(player1, "Sell-Sword Brute");
        Permanent blocker = addCreatureReady(player2, new Sewerdreg());
        resolveCombat(brute, blocker);
    }

    private void resolveCombat(Permanent attacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();
    }
}
