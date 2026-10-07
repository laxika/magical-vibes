package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplinterLeoFatherSon.class, GrizzlyBears.class})
class SplinterLeoFatherSonTest extends BaseCardTest {

    private static final String TOKEN_MODE =
            "Target player creates a 2/2 red Mutant creature token.";
    private static final String COUNTER_MODE =
            "Put a +1/+1 counter on each other creature target player controls.";

    @Test
    void tokenModeCreatesTheTokenUnderTheTargetPlayersControl() {
        castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mutant");
    }

    @Test
    void bothModesTargetDifferentPlayersAndCounterModeExcludesSplinter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent splinter = castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mutant");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(splinter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bothModesMustTargetDifferentPlayers() {
        castSplinter();

        harness.handleListChoice(player1, TOKEN_MODE);
        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());
    }

    @Test
    void counterModeAloneAffectsOnlyOtherCreaturesOfTheTargetPlayer() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent splinter = castSplinter();

        harness.handleListChoice(player1, COUNTER_MODE);
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(splinter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Mutant");
        harness.assertNotOnBattlefield(player2, "Mutant");
    }

    private Permanent castSplinter() {
        harness.castFromHand(player1, new SplinterLeoFatherSon(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        return findPermanent(player1, "Splinter & Leo, Father & Son");
    }
}
