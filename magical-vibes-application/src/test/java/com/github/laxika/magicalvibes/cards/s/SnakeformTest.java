package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class SnakeformTest extends BaseCardTest {

    @Test
    @DisplayName("Sets the target creature's base power and toughness to 1/1")
    void makesTargetOneOne() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4

        castSnakeform(elemental.getId());

        assertThat(elemental.getEffectivePower()).isEqualTo(1);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Strips the target creature's abilities until end of turn")
    void stripsAbilities() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // flying
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

        castSnakeform(elemental.getId());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Turns the target creature green, replacing its other colors")
    void becomesGreen() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // blue
        assertThat(gqs.hasColor(gd, elemental, CardColor.BLUE)).isTrue();

        castSnakeform(elemental.getId());

        assertThat(gqs.hasColor(gd, elemental, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, elemental, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Turns the target creature into a Snake, replacing its other creature types")
    void becomesSnake() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castSnakeform(elemental.getId());

        assertThat(elemental.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.SNAKE);
    }

    @Test
    @DisplayName("Controller draws a card")
    void drawsACard() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castSnakeform(elemental.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        castSnakeform(elemental.getId());

        assertThat(elemental.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, elemental, CardColor.BLUE)).isTrue();
        assertThat(elemental.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears()); // a legal target exists, so the spell is playable
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castSnakeform(UUID targetId) {
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
