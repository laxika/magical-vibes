package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShimatsuTheBloodcloaked.class, DevotedRetainer.class, Forest.class})
class ShimatsuTheBloodcloakedTest extends BaseCardTest {

    private void castShimatsu() {
        harness.castFromHand(player1, new ShimatsuTheBloodcloaked(), "{3}{R}");
    }

    @Test
    @DisplayName("Sacrificing two permanents of any type gives two +1/+1 counters")
    void sacrificingTwoPermanentsAddsTwoCounters() {
        Permanent retainer = harness.addToBattlefieldAndReturn(player1, new DevotedRetainer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(retainer.getId(), forest.getId()));

        assertThat(findPermanent(player1, "Shimatsu the Bloodcloaked")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Devoted Retainer")).isZero();
        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    @DisplayName("Only its controller's permanents are offered for sacrifice")
    void onlyControllerPermanentsAreSacrificeable() {
        Permanent retainer = harness.addToBattlefieldAndReturn(player1, new DevotedRetainer());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castShimatsu();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(retainer.getId(), ownForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(retainer.getId()));

        assertThat(findPermanent(player1, "Shimatsu the Bloodcloaked")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Devoted Retainer")).isZero();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing nothing leaves it a 0/0 that dies to state-based actions")
    void sacrificingNothingLetsItDie() {
        harness.addToBattlefieldAndReturn(player1, new DevotedRetainer());

        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Shimatsu the Bloodcloaked")).isZero();
        assertThat(countPermanents(player1, "Devoted Retainer")).isEqualTo(1);
    }

    @Test
    @DisplayName("With no other permanents there is no prompt and it dies as a 0/0")
    void noOtherPermanentsNoPrompt() {
        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Shimatsu the Bloodcloaked")).isZero();
    }
}
