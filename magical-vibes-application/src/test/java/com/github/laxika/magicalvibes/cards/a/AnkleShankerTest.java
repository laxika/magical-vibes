package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnkleShanker.class, WetlandSambar.class})
class AnkleShankerTest extends BaseCardTest {

    @Test
    void attackGrantsFirstStrikeAndDeathtouchToOwnCreatures() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownCreature = addCreatureReady(player1, new WetlandSambar());
        Permanent opponentCreature = addCreatureReady(player2, new WetlandSambar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownCreature = addCreatureReady(player1, new WetlandSambar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void keywordsAreNotGrantedUntilAttackTriggerResolves() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownCreature = addCreatureReady(player1, new WetlandSambar());

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void creaturesEnteringBeforeResolutionGainKeywordsButLaterCreaturesDoNot() {
        addCreatureReady(player1, new AnkleShanker());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WetlandSambar());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void attackTriggerResolvesEvenIfAnkleShankerLeavesBattlefield() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownCreature = addCreatureReady(player1, new WetlandSambar());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ankleShanker);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ankle Shanker");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTriggerAnkleShanker() {
        Permanent ankleShanker = addCreatureReady(player1, new AnkleShanker());
        Permanent ownCreature = addCreatureReady(player1, new WetlandSambar());
        addCreatureReady(player2, new WetlandSambar());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ankleShanker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }
}
