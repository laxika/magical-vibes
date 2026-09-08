package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FacesOfThePast.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class FacesOfThePastTest extends BaseCardTest {

    @Test
    void tapsMatchingCreaturesWhenThatModeIsChosen() {
        harness.addToBattlefield(player1, new FacesOfThePast());
        Permanent matchingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonmatchingCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());

        killWithShock(dyingCreature);
        harness.handleListChoice(player1,
                "Tap all untapped creatures that share a creature type with it");
        harness.passBothPriorities();

        assertThat(matchingCreature.isTapped()).isTrue();
        assertThat(nonmatchingCreature.isTapped()).isFalse();
    }

    @Test
    void untapsMatchingCreaturesWhenThatModeIsChosen() {
        harness.addToBattlefield(player1, new FacesOfThePast());
        Permanent matchingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonmatchingCreature = addCreatureReady(player1, new LlanowarElves());
        matchingCreature.tap();
        nonmatchingCreature.tap();
        Permanent dyingCreature = addCreatureReady(player2, new GrizzlyBears());

        killWithShock(dyingCreature);
        harness.handleListChoice(player1,
                "Untap all tapped creatures that share a creature type with it");
        harness.passBothPriorities();

        assertThat(matchingCreature.isTapped()).isFalse();
        assertThat(nonmatchingCreature.isTapped()).isTrue();
    }

    private void killWithShock(Permanent creature) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
