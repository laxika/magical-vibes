package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfEssenceTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to Wall of Essence")
    void gainsLifeFromCombatDamage() {
        harness.addToBattlefield(player2, new WallOfEssence());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent wall = gd.playerBattlefields.get(player2.getId()).getFirst();
        wall.setSummoningSick(false);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Wall of Essence");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Wall of Essence")
    void ignoresNoncombatDamage() {
        harness.addToBattlefield(player2, new WallOfEssence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Wall of Essence"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
