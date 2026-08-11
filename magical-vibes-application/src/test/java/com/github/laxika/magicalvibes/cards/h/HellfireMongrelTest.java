package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellfireMongrelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage during an opponent's upkeep when they have two cards in hand")
    void dealsDamageWhenOpponentHasTwoCards() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger when the opponent has more than two cards in hand")
    void doesNotTriggerWithMoreThanTwoCards() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing if the opponent draws above two cards before resolution")
    void conditionIsCheckedAgainAtResolution() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Shock()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
