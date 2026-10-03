package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArsenalThresher.class, DarksteelRelic.class, Ornithopter.class, GrizzlyBears.class})
class ArsenalThresherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters when no other artifact cards are in hand")
    void entersWithNoCountersWhenNoArtifacts() {
        harness.setHand(player1, List.of(new ArsenalThresher()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Arsenal Thresher");
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other artifact card in hand")
    void entersWithCounterPerArtifactCard() {
        harness.setHand(player1, List.of(
                new ArsenalThresher(), new DarksteelRelic(), new Ornithopter()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Arsenal Thresher");
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-artifact cards in hand are not counted")
    void doesNotCountNonArtifactCards() {
        harness.setHand(player1, List.of(
                new ArsenalThresher(), new DarksteelRelic(), new GrizzlyBears()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Arsenal Thresher");
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the controller's hand is counted, not the opponent's artifact cards")
    void doesNotCountOpponentArtifactCards() {
        harness.setHand(player1, List.of(new ArsenalThresher()));
        harness.setHand(player2, List.of(new DarksteelRelic(), new Ornithopter()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Arsenal Thresher");
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // {2}{W/B}{U}: blue covers {U} and the two generic, white pays the {W/B} hybrid.
    private void payMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Controller chooses which other artifact cards to reveal as it enters")
    void offersRevealChoiceInsteadOfAutomaticallyCountingAllArtifacts() {
        harness.setHand(player1, List.of(new ArsenalThresher(), new ArsenalThresher()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Artifacts on the battlefield do not provide entry counters")
    void doesNotCountBattlefieldArtifacts() {
        harness.addToBattlefield(player1, new ArsenalThresher());
        harness.setHand(player1, List.of(new ArsenalThresher()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Arsenal Thresher")).hasSize(2)
                .allSatisfy(permanent -> assertThat(
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
