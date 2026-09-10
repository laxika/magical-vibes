package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({WargTactics.class, GrizzlyBears.class, SerraAngel.class})
class WargTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a creature with flying")
    void destroysFlyingCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        cast(0, angel.getId());

        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("The destroy mode cannot target a creature without flying")
    void destroyModeRejectsNonFlyingCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("The counter mode boosts a creature you control and grants trample and hexproof")
    void boostsControlledCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, bear.getId());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The counter mode's temporary keywords wear off at cleanup")
    void temporaryKeywordsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The counter mode cannot target an opponent's creature")
    void counterModeRejectsOpponentCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(int mode, java.util.UUID targetId) {
        prepareCard();
        harness.castModalInstant(player1, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new WargTactics()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
