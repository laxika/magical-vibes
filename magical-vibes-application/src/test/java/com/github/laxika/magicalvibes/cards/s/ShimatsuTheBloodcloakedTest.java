package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShimatsuTheBloodcloakedTest extends BaseCardTest {

    private void castShimatsu() {
        harness.setHand(player1, new ArrayList<>(List.of(new ShimatsuTheBloodcloaked())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
    }

    private long shimatsuCount() {
        return countPermanents(player1, "Shimatsu the Bloodcloaked");
    }

    @Test
    @DisplayName("Sacrificing two permanents of any type gives two +1/+1 counters")
    void sacrificingTwoPermanentsAddsTwoCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), mountain.getId()));

        assertThat(findPermanent(player1, "Shimatsu the Bloodcloaked")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Mountain")).isZero();
    }

    @Test
    @DisplayName("Sacrificing nothing leaves it a 0/0 that dies to state-based actions")
    void sacrificingNothingLetsItDie() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(shimatsuCount()).isZero();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("With no other permanents there is no prompt and it dies as a 0/0")
    void noOtherPermanentsNoPrompt() {
        castShimatsu();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(shimatsuCount()).isZero();
    }
}
