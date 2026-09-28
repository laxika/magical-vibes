package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
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

@CardUsed({Quickchange.class, Watchwolf.class, Forest.class})
class QuickchangeTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a target creature to one chosen color and draws a card")
    void changesColorAndDraws() {
        Permanent wolf = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, wolf)).containsExactly(CardColor.RED);
        harness.assertInHand(player1, "Watchwolf");
    }

    @Test
    @DisplayName("A target creature can become several chosen colors")
    void changesToSeveralColors() {
        Permanent wolf = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectiveColors(gd, wolf))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent wolf = castQuickchangeOnCreature();

        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");
        assertThat(gqs.getEffectiveColors(gd, wolf)).containsExactly(CardColor.BLUE);

        wolf.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectiveColors(gd, wolf))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
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
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new Watchwolf());
        harness.setLibrary(player1, List.of(new Watchwolf()));
        harness.setHand(player1, List.of(new Quickchange()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, wolf.getId());
        return wolf;
    }
}
