package com.github.laxika.magicalvibes.cards.t;

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

class TurnToFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Sets the target creature's base power and toughness to 1/1")
    void makesTargetOneOne() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4

        castTurnToFrog(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Strips the target creature's abilities")
    void stripsAbilities() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // flying, vigilance
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();

        castTurnToFrog(angel.getId());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Turns the target creature blue, replacing its other colors, and into a Frog")
    void becomesBlueFrog() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // white
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();

        castTurnToFrog(angel.getId());

        assertThat(gqs.hasColor(gd, angel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.FROG);
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castTurnToFrog(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castTurnToFrog(UUID targetId) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
