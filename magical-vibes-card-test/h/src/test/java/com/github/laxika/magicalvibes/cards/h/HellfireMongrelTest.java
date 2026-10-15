package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellfireMongrel.class, Forest.class})
class HellfireMongrelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage during an opponent's upkeep when they have two cards in hand")
    void dealsDamageWhenOpponentHasTwoCards() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger when the opponent has more than two cards in hand")
    void doesNotTriggerWithMoreThanTwoCards() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing if the opponent draws above two cards before resolution")
    void conditionIsCheckedAgainAtResolution() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void dealsDamageWhenOpponentHasNoCards() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void doesNotTriggerDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void losingCardsAfterUpkeepDoesNotCreateATrigger() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of(new Forest()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void eachMongrelTriggersIndependently() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of(new Forest()));
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new HellfireMongrel());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
