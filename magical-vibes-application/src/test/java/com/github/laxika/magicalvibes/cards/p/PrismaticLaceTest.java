package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismaticLace.class, BayFalcon.class, Forest.class})
class PrismaticLaceTest extends BaseCardTest {

    @Test
    @DisplayName("A single chosen color replaces the target's colors (CR 105.3)")
    void singleChosenColorReplacesColors() {
        Permanent falcon = castLaceOnBayFalcon();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, falcon)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Several chosen colors make the target all of those colors")
    void multipleChosenColorsReplaceColors() {
        Permanent falcon = castLaceOnBayFalcon();

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, falcon))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change lasts indefinitely — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent falcon = castLaceOnBayFalcon();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectiveColors(gd, falcon)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("A later color change replaces an earlier indefinite color change")
    void laterColorChangeReplacesEarlierColorChange() {
        Permanent falcon = castLaceOnBayFalcon();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, falcon.getId());
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, falcon)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Any permanent is a legal target, including a land")
    void canTargetALand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Choosing all five colors completes the spell")
    void canChooseAllFiveColors() {
        Permanent falcon = castLaceOnBayFalcon();

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.getEffectiveColors(gd, falcon)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("At least one color must be chosen before finishing")
    void cannotFinishWithoutChoosingAColor() {
        Permanent falcon = castLaceOnBayFalcon();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "DONE"))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, falcon)).containsExactly(CardColor.RED);
    }

    private Permanent castLaceOnBayFalcon() {
        Permanent falcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());
        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, falcon.getId());

        return falcon;
    }
}
