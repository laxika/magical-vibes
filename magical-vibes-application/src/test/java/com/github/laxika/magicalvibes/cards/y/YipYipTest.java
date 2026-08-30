package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AgadeemOccultist;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YipYip.class, AgadeemOccultist.class, GrizzlyBears.class, FountainOfYouth.class})
class YipYipTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature you control +2/+2 and an Ally flying")
    void boostsAllyAndGrantsFlying() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isEqualTo(2);
        assertThat(ally.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gives a non-Ally creature only +2/+2")
    void boostsNonAllyWithoutFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AgadeemOccultist());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, ally.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getPowerModifier()).isZero();
        assertThat(ally.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, java.util.List.of(new YipYip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
