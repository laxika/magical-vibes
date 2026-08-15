package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenegadeFreighterTest extends BaseCardTest {

    @Test
    void crewAnimatesFreighterAndTapsTheCrewedCreature() {
        Permanent freighter = addFreighterReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingFreighterGetsPlusOnePlusOneAndTrampleUntilEndOfTurn() {
        Permanent freighter = addFreighterReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, freighter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, freighter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, freighter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, freighter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addFreighterReady(Player player) {
        Permanent permanent = new Permanent(new RenegadeFreighter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
