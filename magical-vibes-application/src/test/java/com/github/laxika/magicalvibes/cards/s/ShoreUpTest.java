package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShoreUp.class, GrizzlyBears.class, GiantGrowth.class})
class ShoreUpTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a creature you control, gives it +1/+1, and grants hexproof")
    void untapsBoostsAndGrantsHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        castShoreUp(creature);

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("The target cannot be targeted by an opponent")
    void preventsOpponentTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castShoreUp(creature);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castShoreUp(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.HEXPROOF);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShoreUp(Permanent creature) {
        harness.setHand(player1, List.of(new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
