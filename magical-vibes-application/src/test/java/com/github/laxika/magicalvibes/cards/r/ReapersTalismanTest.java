package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReapersTalisman.class, GrizzlyBears.class})
class ReapersTalismanTest extends BaseCardTest {

    @Test
    void attackingAloneGivesDeathtouchAndDrainsDefendingPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void attackingWithAnotherCreatureOnlyGivesDeathtouch() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent talisman = addTalismanReady(player1);
        talisman.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    private Permanent addTalismanReady(Player player) {
        Permanent talisman = new Permanent(new ReapersTalisman());
        talisman.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(talisman);
        return talisman;
    }
}
