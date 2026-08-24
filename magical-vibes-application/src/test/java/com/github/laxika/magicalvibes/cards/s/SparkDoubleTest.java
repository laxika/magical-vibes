package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkDouble.class, ChoMannoRevolutionary.class, JaceBeleren.class, GrizzlyBears.class})
class SparkDoubleTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a controlled creature with an additional +1/+1 counter")
    void copiesControlledCreatureWithCounterAndNoLegendarySupertype() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChoMannoRevolutionary());

        Permanent copy = castAndChoose(target);

        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copies a controlled planeswalker with its printed loyalty plus one")
    void copiesControlledPlaneswalkerWithAdditionalLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);

        Permanent copy = castAndChoose(target);

        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot copy a permanent controlled by an opponent")
    void cannotCopyOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SparkDouble sparkDouble = new SparkDouble();
        castSparkDouble(sparkDouble);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(sparkDouble.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sparkDouble.getId()));
    }

    private Permanent castAndChoose(Permanent target) {
        SparkDouble sparkDouble = new SparkDouble();
        castSparkDouble(sparkDouble);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(sparkDouble.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void castSparkDouble(SparkDouble sparkDouble) {
        harness.setHand(player1, List.of(sparkDouble));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
