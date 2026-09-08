package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SellSwordBrute.class, GrizzlyBears.class})
class SellSwordBruteTest extends BaseCardTest {

    @Test
    @DisplayName("When Sell-Sword Brute dies, it deals 2 damage to its controller")
    void deathTriggerDamagesController() {
        harness.addToBattlefield(player1, new SellSwordBrute());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killSellSwordBrute();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Sell-Sword Brute");
    }

    private void killSellSwordBrute() {
        Permanent brute = findPermanent(player1, "Sell-Sword Brute");
        brute.setSummoningSick(false);
        brute.setAttacking(true);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
