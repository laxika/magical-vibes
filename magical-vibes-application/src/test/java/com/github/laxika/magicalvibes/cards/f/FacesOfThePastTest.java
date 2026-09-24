package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FacesOfThePast.class, GoblinBrigand.class, ScornfulEgotist.class, SparkSpray.class})
class FacesOfThePastTest extends BaseCardTest {

    @Test
    void tapsMatchingCreaturesWhenThatModeIsChosen() {
        harness.addToBattlefield(player1, new FacesOfThePast());
        Permanent matchingCreature = addCreatureReady(player1, new ScornfulEgotist());
        Permanent nonmatchingCreature = addCreatureReady(player1, new GoblinBrigand());
        Permanent dyingCreature = addCreatureReady(player2, new ScornfulEgotist());

        killWithSparkSpray(dyingCreature);
        harness.handleListChoice(player1,
                "Tap all untapped creatures that share a creature type with it");
        harness.passBothPriorities();

        assertThat(matchingCreature.isTapped()).isTrue();
        assertThat(nonmatchingCreature.isTapped()).isFalse();
    }

    @Test
    void untapsMatchingCreaturesWhenThatModeIsChosen() {
        harness.addToBattlefield(player1, new FacesOfThePast());
        Permanent matchingCreature = addCreatureReady(player1, new ScornfulEgotist());
        Permanent nonmatchingCreature = addCreatureReady(player1, new GoblinBrigand());
        matchingCreature.tap();
        nonmatchingCreature.tap();
        Permanent dyingCreature = addCreatureReady(player2, new ScornfulEgotist());

        killWithSparkSpray(dyingCreature);
        harness.handleListChoice(player1,
                "Untap all tapped creatures that share a creature type with it");
        harness.passBothPriorities();

        assertThat(matchingCreature.isTapped()).isFalse();
        assertThat(nonmatchingCreature.isTapped()).isTrue();
    }

    @Test
    void affectsMatchingCreaturesOnBothBattlefields() {
        harness.addToBattlefield(player1, new FacesOfThePast());
        Permanent ownMatchingCreature = addCreatureReady(player1, new ScornfulEgotist());
        Permanent opponentMatchingCreature = addCreatureReady(player2, new ScornfulEgotist());
        Permanent opponentNonmatchingCreature = addCreatureReady(player2, new GoblinBrigand());
        Permanent dyingCreature = addCreatureReady(player2, new ScornfulEgotist());

        killWithSparkSpray(dyingCreature);
        harness.handleListChoice(player1,
                "Tap all untapped creatures that share a creature type with it");
        harness.passBothPriorities();

        assertThat(ownMatchingCreature.isTapped()).isTrue();
        assertThat(opponentMatchingCreature.isTapped()).isTrue();
        assertThat(opponentNonmatchingCreature.isTapped()).isFalse();
    }

    private void killWithSparkSpray(Permanent creature) {
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
