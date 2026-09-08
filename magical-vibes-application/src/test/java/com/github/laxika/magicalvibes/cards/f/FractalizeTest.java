package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FractalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature a green and blue Fractal with X plus 1 power and toughness")
    void makesTargetFractal() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFractalize(elemental.getId(), 3);

        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, elemental, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, elemental, CardColor.BLUE)).isTrue();
        assertThat(elemental.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.FRACTAL);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removes the target creature's previous color")
    void replacesPreviousColor() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castFractalize(angel.getId(), 0);

        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("All changes wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFractalize(elemental.getId(), 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, elemental, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, elemental, CardColor.GREEN)).isFalse();
        assertThat(elemental.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fractalize()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFractalize(UUID targetId, int xValue) {
        harness.setHand(player1, List.of(new Fractalize()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 1);
        harness.castInstant(player1, 0, xValue, targetId);
        harness.passBothPriorities();
    }
}
