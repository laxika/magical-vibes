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

    @Test
    @DisplayName("The destroy mode can destroy your own flying creature")
    void destroysOwnFlyingCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        cast(0, angel.getId());

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("The counter mode affects only its target and does not destroy a flying target")
    void counterModeAffectsOnlyItsTarget() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, angel.getId());

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(angel.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(angel.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof granted in response makes an opponent's destroy mode fail to resolve")
    void hexproofProtectsAgainstPendingDestroyMode() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player2, List.of(new WargTactics()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player2, 0, 0, List.of(angel.getId()));

        prepareCard();
        harness.castModalInstant(player1, 0, 1, List.of(angel.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(angel.hasKeyword(Keyword.HEXPROOF)).isTrue();
        harness.assertInGraveyard(player2, "Warg Tactics");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof does not stop its controller from targeting the creature again")
    void controllerCanTargetHexproofCreatureAgain() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, bear.getId());
        cast(1, bear.getId());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.HEXPROOF)).isTrue();
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
