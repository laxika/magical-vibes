package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfThePyromancer.class, Forest.class, Panopticon.class})
class PathOfThePyromancerTest extends BaseCardTest {

    @BeforeEach
    void preparePlanechase() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Panopticon(), gd.nextTimestamp()));
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void addsRedManaForDiscardedCardsThenDrawsOneMore() {
        PathOfThePyromancer spell = new PathOfThePyromancer();
        Card discardedOne = new Forest();
        Card discardedTwo = new Forest();
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of(spell, discardedOne, discardedTwo));
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedOne, discardedTwo);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();
    }

    @Test
    void emptyHandStillDrawsOneCardWithoutAddingMana() {
        PathOfThePyromancer spell = new PathOfThePyromancer();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        harness.handleListChoice(player1, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.handleListChoice(player2, ChoiceContext.WillOfThePlaneswalkersChoice.CHAOS);
        harness.passBothPriorities();
    }
}
