package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BattleHurdaTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        harness.addToBattlefield(player1, new BattleHurda());

        Permanent permanent = findPermanent(player1, "Battle Hurda");

        assertThat(permanent.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike deals damage before a smaller blocker")
    void firstStrikeDealsDamageBeforeSmallerBlocker() {
        BattleHurda hurda = new BattleHurda();
        Permanent attacker = new Permanent(hurda);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(2);
        blockerCard.setToughness(2);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Battle Hurda");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
