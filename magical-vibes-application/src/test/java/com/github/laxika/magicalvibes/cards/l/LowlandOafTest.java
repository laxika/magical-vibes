package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LowlandOafTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and flying to a target Goblin you control")
    void pumpsAndGrantsFlying() {
        Permanent oaf = new Permanent(new LowlandOaf());
        oaf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(oaf);

        Permanent goblin = new Permanent(new FesteringGoblin());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        int basePower = gqs.getEffectivePower(gd, goblin);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, goblin.getId());
        assertThat(after.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Target Goblin is sacrificed at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent oaf = new Permanent(new LowlandOaf());
        oaf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(oaf);

        Permanent goblin = new Permanent(new FesteringGoblin());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        // Still on the battlefield during the main phase.
        harness.assertOnBattlefield(player1, "Festering Goblin");

        // Advance to the end step — the Goblin should be sacrificed.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Festering Goblin");
        harness.assertInGraveyard(player1, "Festering Goblin");
    }

    @Test
    @DisplayName("Cannot target a non-Goblin creature")
    void cannotTargetNonGoblin() {
        Permanent oaf = new Permanent(new LowlandOaf());
        oaf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(oaf);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Goblin")
    void cannotTargetOpponentGoblin() {
        Permanent oaf = new Permanent(new LowlandOaf());
        oaf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(oaf);

        Permanent opponentGoblin = new Permanent(new FesteringGoblin());
        gd.playerBattlefields.get(player2.getId()).add(opponentGoblin);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
