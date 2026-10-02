package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DestructorDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtarkaWorldRender.class, DestructorDragon.class, AleshasVanguard.class})
class AtarkaWorldRenderTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking Dragon gains double strike until end of turn")
    void attackingDragonGainsDoubleStrike() {
        Permanent atarka = addCreatureReady(player1, new AtarkaWorldRender());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, atarka, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Each attacking Dragon gets double strike, but a non-Dragon does not")
    void onlyAttackingDragonsGainDoubleStrike() {
        addCreatureReady(player1, new AtarkaWorldRender());
        Permanent dragon = addCreatureReady(player1, new DestructorDragon());
        Permanent nonDragon = addCreatureReady(player1, new AleshasVanguard());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonDragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger Atarka")
    void nonDragonDoesNotTrigger() {
        addCreatureReady(player1, new AtarkaWorldRender());
        Permanent nonDragon = addCreatureReady(player1, new AleshasVanguard());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, nonDragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent atarka = addCreatureReady(player1, new AtarkaWorldRender());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, atarka, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, atarka, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opposing attacking Dragon does not trigger Atarka")
    void opposingDragonDoesNotGainDoubleStrike() {
        addCreatureReady(player1, new AtarkaWorldRender());
        Permanent dragon = addCreatureReady(player2, new DestructorDragon());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An attack trigger still grants double strike after Atarka leaves")
    void triggerResolvesWithoutAtarka() {
        Permanent atarka = addCreatureReady(player1, new AtarkaWorldRender());
        Permanent dragon = addCreatureReady(player1, new DestructorDragon());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(atarka);
        gd.playerGraveyards.get(player1.getId()).add(atarka.getCard());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Each attacking Dragon gains double strike while a nonattacking Dragon does not")
    void multipleRealDragonAttackersGainDoubleStrike() {
        Permanent atarka = addCreatureReady(player1, new AtarkaWorldRender());
        Permanent dragon = addCreatureReady(player1, new DestructorDragon());
        Permanent restingDragon = addCreatureReady(player1, new DestructorDragon());
        Permanent nonDragon = addCreatureReady(player1, new AleshasVanguard());

        declareAttackers(List.of(0, 1, 3));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, atarka, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, restingDragon, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonDragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

}
