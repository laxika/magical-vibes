package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderUK.class, GrizzlyBears.class, Forest.class})
class SpiderUKTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {2}{W} by returning a tapped creature you control")
    void castsForWebSlingingCost() {
        var tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SpiderUK()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-UK");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Web-slinging requires a tapped creature")
    void requiresTappedCreature() {
        var untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderUK()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Draws a card and gains 2 life when two creatures entered this turn")
    void drawsAndGainsLifeAfterTwoCreaturesEnter() {
        harness.setHand(player1, List.of(new SpiderUK(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when only Spider-UK entered this turn")
    void doesNotTriggerWithOnlyOneCreatureEntering() {
        harness.setHand(player1, List.of(new SpiderUK()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        int handSize = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.assertLife(player1, 20);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
