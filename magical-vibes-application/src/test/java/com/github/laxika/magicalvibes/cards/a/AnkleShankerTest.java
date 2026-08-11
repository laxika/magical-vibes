package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnkleShankerTest extends BaseCardTest {

    @Test
    void attackGrantsFirstStrikeAndDeathtouchToOwnCreatures() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.DEATHTOUCH)).isFalse();
    }
}
