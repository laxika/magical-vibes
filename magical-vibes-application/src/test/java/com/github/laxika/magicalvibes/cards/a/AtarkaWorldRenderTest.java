package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent dragon = addCreatureReady(player1, createCreature("Test Dragon", CardSubtype.DRAGON));
        Permanent nonDragon = addCreatureReady(player1, createCreature("Test Creature"));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonDragon, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger Atarka")
    void nonDragonDoesNotTrigger() {
        addCreatureReady(player1, new AtarkaWorldRender());
        Permanent nonDragon = addCreatureReady(player1, createCreature("Test Creature"));

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

    private Card createCreature(String name, CardSubtype... subtypes) {
        Card creature = new Card();
        creature.setName(name);
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{R}");
        creature.setColor(CardColor.RED);
        creature.setSubtypes(List.of(subtypes));
        creature.setPower(2);
        creature.setToughness(2);
        return creature;
    }
}
