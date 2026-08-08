package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Herald of Faith gains its controller 2 life")
    void attackGainsTwoLife() {
        Permanent herald = new Permanent(new HeraldOfFaith());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Herald of Faith staying home gains no life")
    void noAttackNoLife() {
        Permanent herald = new Permanent(new HeraldOfFaith());
        herald.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(herald);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only the bears attack
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
