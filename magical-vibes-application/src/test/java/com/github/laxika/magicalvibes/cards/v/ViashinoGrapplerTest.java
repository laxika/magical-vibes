package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViashinoGrapplerTest extends BaseCardTest {

    @Test
    void gainsTrampleUntilEndOfTurn() {
        Permanent grappler = addGrapplerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grappler, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grappler, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addGrapplerReady(Player player) {
        Permanent grappler = new Permanent(new ViashinoGrappler());
        grappler.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(grappler);
        return grappler;
    }
}
