package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CennsTactician;
import com.github.laxika.magicalvibes.cards.w.WarrenWeirding;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinsbaileBorderguard.class, CennsTactician.class, WarrenWeirding.class})
class KinsbaileBorderguardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with 0 counters when no other Kithkin are controlled")
    void entersWithNoCountersWithoutOtherKithkin() {
        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent borderguard = findPermanent(player1, "Kinsbaile Borderguard");
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other Kithkin controlled")
    void entersWithCountersPerOtherKithkin() {
        harness.addToBattlefield(player1, new CennsTactician());
        harness.addToBattlefield(player1, new CennsTactician());

        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent borderguard = findPermanent(player1, "Kinsbaile Borderguard");
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count the Borderguard itself or opponent's Kithkin")
    void doesNotCountSelfOrOpponentKithkin() {
        harness.addToBattlefield(player2, new CennsTactician());

        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent borderguard = findPermanent(player1, "Kinsbaile Borderguard");
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("When it dies, creates a 1/1 white Kithkin Soldier token for each counter on it")
    void deathCreatesTokenPerCounter() {
        Permanent borderguard = harness.addToBattlefieldAndReturn(player1, new KinsbaileBorderguard());
        borderguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Kinsbaile Borderguard");
        assertThat(gd.stack).hasSize(1); // death trigger on the stack

        harness.passBothPriorities();

        assertKithkinSoldierTokens(3);
    }

    @Test
    @DisplayName("Counts every counter type when it dies")
    void deathCreatesTokenForEveryCounterType() {
        Permanent borderguard = harness.addToBattlefieldAndReturn(player1, new KinsbaileBorderguard());
        borderguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        borderguard.setCounterCount(CounterType.CHARGE, 2);

        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertKithkinSoldierTokens(3);
    }

    @Test
    @DisplayName("When it dies with no counters, creates no tokens")
    void deathWithNoCountersCreatesNoTokens() {
        harness.addToBattlefield(player1, new KinsbaileBorderguard());

        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(findPermanents(player1, "Kithkin Soldier")).isEmpty();
    }

    private void assertKithkinSoldierTokens(int expectedCount) {
        List<Permanent> tokens = findPermanents(player1, "Kithkin Soldier");
        assertThat(tokens).hasSize(expectedCount);
        for (Permanent token : tokens) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Death ability still triggers when there are no counters")
    void deathWithoutCountersStillPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new KinsbaileBorderguard());
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Kinsbaile Borderguard");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertKithkinSoldierTokens(0);
    }

    @Test
    @DisplayName("Death ability uses the counter count at death")
    void deathAbilitySnapshotsCountersBeforeResolution() {
        Permanent borderguard = harness.addToBattlefieldAndReturn(player1, new KinsbaileBorderguard());
        borderguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.assertInGraveyard(player1, "Kinsbaile Borderguard");
        assertThat(gd.stack).hasSize(1);
        borderguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.passBothPriorities();
        assertKithkinSoldierTokens(2);
    }
}
