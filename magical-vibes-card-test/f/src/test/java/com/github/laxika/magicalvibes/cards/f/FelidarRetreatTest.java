package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelidarRetreat.class, Forest.class, CliffhavenSellSword.class})
class FelidarRetreatTest extends BaseCardTest {

    @Test
    void landfallCreatesCatBeastToken() {
        harness.addToBattlefield(player1, new FelidarRetreat());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 2/2 white Cat Beast creature token.");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Cat Beast");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.CAT, CardSubtype.BEAST);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void landfallPutsCountersAndGrantsVigilanceToOwnCreatures() {
        harness.addToBattlefield(player1, new FelidarRetreat());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn.");
        resolveAllTriggers();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void grantedVigilanceExpiresAtEndOfTurnButCountersRemain() {
        harness.addToBattlefield(player1, new FelidarRetreat());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn.");
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveCountersOrVigilance() {
        harness.addToBattlefield(player1, new FelidarRetreat());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn.");
        resolveAllTriggers();

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, existing, Keyword.VIGILANCE)).isTrue();
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void counterModeCanBeChosenWithoutCreatures() {
        Permanent retreat = harness.addToBattlefieldAndReturn(player1, new FelidarRetreat());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Put a +1/+1 counter on each creature you control. Those creatures gain vigilance until end of turn.");
        resolveAllTriggers();

        assertThat(retreat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, retreat, Keyword.VIGILANCE)).isFalse();
        assertThat(countPermanents(player1, "Cat Beast")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsLandDoesNotTriggerRetreat() {
        harness.addToBattlefield(player1, new FelidarRetreat());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Cat Beast")).isZero();
    }
}
