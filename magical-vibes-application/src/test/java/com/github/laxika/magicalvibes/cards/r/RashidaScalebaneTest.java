package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RashidaScalebaneTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking Dragon and gains life equal to its power")
    void destroysAttackingDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = new Permanent(new ShivanDragon());
        dragon.setSummoningSick(false);
        dragon.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(dragon);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shivan Dragon"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shivan Dragon"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Destroys a blocking Dragon")
    void destroysBlockingDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = new Permanent(new ShivanDragon());
        dragon.setSummoningSick(false);
        dragon.setBlocking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(dragon);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shivan Dragon"));
    }

    @Test
    @DisplayName("Cannot target a Dragon that is neither attacking nor blocking")
    void cannotTargetIdleDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent dragon = addCreatureReady(player2, new ShivanDragon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dragon.getId()))
                .hasMessageContaining("Target must be an attacking or blocking Dragon");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Dragon creature")
    void cannotTargetAttackingNonDragon() {
        addCreatureReady(player1, new RashidaScalebane());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .hasMessageContaining("Target must be an attacking or blocking Dragon");
    }
}
