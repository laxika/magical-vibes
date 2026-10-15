package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EastWindAvatar.class, GrizzlyBears.class})
class EastWindAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 until end of turn when another creature you control enters")
    void getsBoostWhenAllyCreatureEnters() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new EastWindAvatar());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noBoostWhenOpponentCreatureEnters() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new EastWindAvatar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost is cumulative across multiple creature entries")
    void boostStacksForMultipleCreatures() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new EastWindAvatar());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new EastWindAvatar());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotBoostItselfWhenItEnters() {
        harness.castFromHand(player1, new EastWindAvatar(), "{3}{W}");
        harness.passBothPriorities();

        Permanent avatar = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Another avatar boosts only the existing avatar after the trigger resolves")
    void anotherAvatarBoostsOnlyTheExistingAvatar() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new EastWindAvatar());

        harness.castFromHand(player1, new EastWindAvatar(), "{3}{W}");
        harness.passBothPriorities();

        Permanent enteringAvatar = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enteringAvatar)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, avatar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, avatar)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, enteringAvatar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteringAvatar)).isEqualTo(4);
    }
}
