package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismaticLaceTest extends BaseCardTest {

    @Test
    @DisplayName("A single chosen color replaces the target's colors (CR 105.3)")
    void singleChosenColorReplacesColors() {
        Permanent bears = castLaceOnBears();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Several chosen colors make the target all of those colors")
    void multipleChosenColorsReplaceColors() {
        Permanent bears = castLaceOnBears();

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change lasts indefinitely — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent bears = castLaceOnBears();

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");

        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Any permanent is a legal target, including a land")
    void canTargetALand() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Forest"));
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "DONE");

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
    }

    private Permanent castLaceOnBears() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrismaticLace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        return findPermanent(player2, "Grizzly Bears");
    }
}
