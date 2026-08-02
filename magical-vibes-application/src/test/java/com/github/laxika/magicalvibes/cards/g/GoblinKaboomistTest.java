package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinKaboomistTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep creates a Land Mine token and flips a coin")
    void upkeepCreatesLandMineAndFlips() {
        harness.addToBattlefield(player1, new GoblinKaboomist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(landMineIndex()).isNotNegative();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Goblin Kaboomist"));
    }

    @Test
    @DisplayName("Losing the flip deals 2 damage to Goblin Kaboomist, killing it; winning leaves it alone")
    void lostFlipDamagesItself() {
        harness.addToBattlefield(player1, new GoblinKaboomist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        boolean lost = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("loses the coin flip for Goblin Kaboomist"));
        boolean stillAlive = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Goblin Kaboomist"));

        // 1/2 creature: 2 damage is lethal, so a lost flip means it is gone.
        assertThat(stillAlive).isEqualTo(!lost);
    }

    @Test
    @DisplayName("Land Mine deals 2 damage to an attacking creature without flying and is sacrificed")
    void landMineDamagesAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, landMineIndex(), null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(landMineIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Land Mine cannot target an attacking creature with flying")
    void landMineCannotTargetFlyingAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent flier = new Permanent(new AirElemental());
        flier.setSummoningSick(false);
        flier.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(flier);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        int index = landMineIndex();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, flier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature without flying");
    }

    @Test
    @DisplayName("Land Mine cannot target a creature that is not attacking")
    void landMineCannotTargetNonAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bystander);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        int index = landMineIndex();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature without flying");
    }

    /** Index of the Land Mine token on player1's battlefield, or -1 when none is there. */
    private int landMineIndex() {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Land Mine")) {
                return i;
            }
        }
        return -1;
    }
}
