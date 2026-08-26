package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quickchange.class, GrizzlyBears.class, Forest.class})
class QuickchangeTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a target creature to one chosen color and draws a card")
    void changesColorAndDraws() {
        Permanent bears = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A target creature can become several chosen colors")
    void changesToSeveralColors() {
        Permanent bears = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent bears = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLUE);

        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Quickchange()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castQuickchangeOnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Quickchange()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        return bears;
    }
}
