package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErhnamDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets a non-Wall creature an opponent controls")
    void upkeepTriggerTargetsNonWallOpponentCreature() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfStone());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(wall.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Forestwalk lasts through the controller's next upkeep")
    void forestwalkLastsUntilNextUpkeep() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        grantForestwalk(target);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        advanceToUpkeep(player1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    private void grantForestwalk(Permanent target) {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
