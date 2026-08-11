package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerraZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        harness.addToBattlefield(player1, new SerraZealot());

        Permanent zealot = findPermanent(player1, "Serra Zealot");

        assertThat(gqs.hasKeyword(gd, zealot, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike kills a 1/1 blocker before regular combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = new Permanent(new SerraZealot());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(1);
        bearCard.setToughness(1);
        Permanent blocker = new Permanent(bearCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Zealot");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
