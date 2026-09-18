package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CyclopeanTomb.class, Forest.class, Swamp.class, Shatter.class})
class CyclopeanTombTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a mire counter on a non-Swamp land and makes it a Swamp")
    void putsMireCounterAndGrantsSwamp() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new CyclopeanTomb());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(tomb.isTapped()).isTrue();
        assertThat(forest.getCounterCount(CounterType.MIRE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.SWAMP)).isTrue();

        forest.setCounterCount(CounterType.MIRE, 0);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.SWAMP)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a Swamp")
    void cannotTargetSwamp() {
        harness.addToBattlefieldAndReturn(player1, new CyclopeanTomb());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Swamp land");
    }

    @Test
    @DisplayName("After leaving the battlefield, removes mire counters from a remembered land at upkeep")
    void removesRememberedMireCountersAfterLeavingBattlefield() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new CyclopeanTomb());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, tomb.getId());
        resolveAllTriggers();

        assertThat(tomb).isNotIn(gd.playerBattlefields.get(player1.getId()));
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(forest.getCounterCount(CounterType.MIRE)).isZero();
    }
}
