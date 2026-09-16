package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringSliver.class, MetallicSliver.class, GrizzlyBears.class})
class EnduringSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Enduring Sliver can outlast itself at sorcery speed")
    void outlastsItself() {
        Permanent enduringSliver = addCreatureReady(player1, new EnduringSliver());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(enduringSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enduringSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other Sliver creatures you control gain outlast")
    void grantsOutlastToOtherSliversYouControl() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());
        Permanent nonSliver = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();

        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastIsSorcerySpeed() {
        addCreatureReady(player1, new EnduringSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareForSorceryAction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
