package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.SkyriderPatrol;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiamondMare.class, GreenwoodSentinel.class, SnappingDrake.class, SkyriderPatrol.class})
class DiamondMareTest extends BaseCardTest {

    private void castAndChooseColor(CardColor color) {
        harness.castFromHand(player1, new DiamondMare(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, color.name());
    }

    @Test
    @DisplayName("Casting a spell of the chosen color gains 1 life")
    void gainsLifeForChosenColorSpell() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Casting a spell of another color does not gain life")
    void doesNotGainLifeForAnotherColorSpell() {
        castAndChooseColor(CardColor.RED);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new SnappingDrake(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void triggerResolvesBeforeTheMatchingSpell() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
    }

    @Test
    void multicoloredSpellGainsOnlyOneLife() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new SkyriderPatrol(), "{2}{G}{U}");
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsMatchingSpellDoesNotGainLife() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GreenwoodSentinel(), "{1}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    void colorlessSpellDoesNotGainLife() {
        castAndChooseColor(CardColor.GREEN);
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new DiamondMare(), "{2}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.assertLife(player1, 10);
    }
}
