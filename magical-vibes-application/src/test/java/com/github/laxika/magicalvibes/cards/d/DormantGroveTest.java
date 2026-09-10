package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GnarledGrovestrider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DormantGrove.class, GnarledGrovestrider.class, ColossalDreadmaw.class, GrizzlyBears.class})
class DormantGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a counter on a creature and transforms with sufficient toughness")
    void transformsWhenTargetHasAtLeastSixToughness() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());

        resolveBeginningOfCombat(dreadmaw);

        assertThat(dreadmaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Beginning of combat only puts a counter when the creature remains below six toughness")
    void doesNotTransformWhenTargetHasInsufficientToughness() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveBeginningOfCombat(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat ability targets only a creature the controller controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new DormantGrove());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gnarled Grovestrider gives other creatures you control vigilance")
    void backFaceGrantsVigilanceToOtherOwnCreatures() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        resolveBeginningOfCombat(dreadmaw);

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(grove.isTransformed()).isTrue();
    }

    private void resolveBeginningOfCombat(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
