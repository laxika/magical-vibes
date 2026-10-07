package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormFleetSprinter.class, RaptorCompanion.class})
class StormFleetSprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Storm Fleet Sprinter can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new RaptorCompanion());
        Permanent sprinter = addCreatureReady(player1, new StormFleetSprinter());
        sprinter.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Haste lets Storm Fleet Sprinter attack the turn it enters")
    void hasteAllowsAttackWhileSummoningSick() {
        Permanent sprinter = harness.addToBattlefieldAndReturn(player1, new StormFleetSprinter());
        assertThat(sprinter.isSummoningSick()).isTrue();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Storm Fleet Sprinter can block while summoning sick")
    void canBlockWhileSummoningSick() {
        Permanent attacker = addCreatureReady(player1, new RaptorCompanion());
        attacker.setAttacking(true);
        Permanent sprinter = harness.addToBattlefieldAndReturn(player2, new StormFleetSprinter());
        assertThat(sprinter.isSummoningSick()).isTrue();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sprinter);
    }
}
