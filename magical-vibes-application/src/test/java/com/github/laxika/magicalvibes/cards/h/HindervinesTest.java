package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HindervinesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with no +1/+1 counters are prevented from dealing combat damage")
    void preventsCreaturesWithoutCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        castHindervines();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
        harness.assertInGraveyard(player1, "Hindervines");
    }

    @Test
    @DisplayName("Creatures with a +1/+1 counter still deal combat damage")
    void exemptsCreaturesWithCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHindervines();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    @Test
    @DisplayName("Counters gained after resolution exempt the creature")
    void countersAddedLaterExempt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        castHindervines();
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    private void castHindervines() {
        harness.setHand(player1, List.of(new Hindervines()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
