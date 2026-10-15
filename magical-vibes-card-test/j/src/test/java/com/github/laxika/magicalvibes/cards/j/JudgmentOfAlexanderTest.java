package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudgmentOfAlexander.class, ColossalDreadmaw.class, GrizzlyBears.class, Shock.class})
class JudgmentOfAlexanderTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents creature damage and each commander retaliates")
    void preventsCreatureDamageAndEachCommanderRetaliates() {
        Permanent commanderOne = addCreatureReady(player1, new GrizzlyBears());
        Permanent commanderTwo = addCreatureReady(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commanderOne.getCard());
        gd.makeCommander(player1.getId(), commanderTwo.getCard());
        castJudgmentOfAlexander();

        Permanent attacker = addCreatureReady(player2, new ColossalDreadmaw());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Prevents noncreature damage without triggering commander retaliation")
    void preventsNoncreatureDamageWithoutRetaliation() {
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commander.getCard());
        castJudgmentOfAlexander();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(commander.getMarkedDamage()).isZero();
    }

    private void castJudgmentOfAlexander() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JudgmentOfAlexander()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
