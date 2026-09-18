package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.m.MirrorWall;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErhnamDjinn.class, AnuridBarkripper.class, MirrorWall.class, KrosanVerge.class})
class ErhnamDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets a non-Wall creature an opponent controls")
    void upkeepTriggerTargetsNonWallOpponentCreature() {
        Permanent source = addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new AnuridBarkripper());
        Permanent wall = addCreatureReady(player2, new MirrorWall());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .doesNotContain(source.getId(), wall.getId(), land.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Forestwalk lasts through the source controller's next upkeep")
    void forestwalkLastsUntilSourceControllerNextUpkeep() {
        addCreatureReady(player1, new ErhnamDjinn());
        Permanent target = addCreatureReady(player2, new AnuridBarkripper());

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
