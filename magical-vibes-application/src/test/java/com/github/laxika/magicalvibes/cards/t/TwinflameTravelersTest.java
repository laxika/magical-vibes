package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FlamekinHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwinflameTravelersTest extends BaseCardTest {

    @Test
    @DisplayName("Another Elemental's triggered ability triggers twice")
    void doublesAnotherElementalsTriggeredAbility() {
        harness.addToBattlefield(player1, new TwinflameTravelers());

        harness.setHand(player1, List.of(new FlamekinHarbinger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A non-Elemental's triggered ability triggers only once")
    void doesNotDoubleNonElementalTriggeredAbility() {
        harness.addToBattlefield(player1, new TwinflameTravelers());
        harness.addToBattlefield(player1, new MentorOfTheMeek());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }
}
