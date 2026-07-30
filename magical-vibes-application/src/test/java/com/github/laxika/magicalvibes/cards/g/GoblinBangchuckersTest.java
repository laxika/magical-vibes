package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinBangchuckersTest extends BaseCardTest {

    @Test
    @DisplayName("Winning the flip deals 2 to the targeted player, losing deals 2 to itself")
    void flipDamagesTargetOrSelf() {
        harness.setLife(player2, 20);
        addReadyBangchuckers(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        if (wonFlip()) {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            harness.assertOnBattlefield(player1, "Goblin Bangchuckers");
        } else {
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            // 2 damage kills the 2/2 itself.
            harness.assertNotOnBattlefield(player1, "Goblin Bangchuckers");
            harness.assertInGraveyard(player1, "Goblin Bangchuckers");
        }
    }

    @Test
    @DisplayName("A won flip kills the targeted 2/2; a lost flip leaves it alive")
    void flipDamagesTargetCreatureOnlyOnWin() {
        addReadyBangchuckers(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        if (wonFlip()) {
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Goblin Bangchuckers");
        } else {
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Goblin Bangchuckers");
        }
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        Permanent permanent = new Permanent(new GoblinBangchuckers());
        gd.playerBattlefields.get(player1.getId()).add(permanent);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private boolean wonFlip() {
        return gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Goblin Bangchuckers"));
    }

    private Permanent addReadyBangchuckers(Player player) {
        Permanent permanent = new Permanent(new GoblinBangchuckers());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
