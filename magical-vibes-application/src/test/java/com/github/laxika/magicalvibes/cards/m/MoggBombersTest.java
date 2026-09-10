package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggBombers.class, MoggFlunkies.class, JaceBeleren.class})
class MoggBombersTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering sacrifices Mogg Bombers and deals 3 damage to a player")
    void anotherCreatureEnters() {
        harness.castFromHand(player1, new MoggBombers(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Mogg Bombers");

        harness.castFromHand(player1, new MoggFlunkies(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        int lifeBefore = gd.getLife(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertNotOnBattlefield(player1, "Mogg Bombers");
        harness.assertInGraveyard(player1, "Mogg Bombers");
        harness.assertOnBattlefield(player1, "Mogg Flunkies");
    }

    @Test
    @DisplayName("An opponent's creature entering also triggers Mogg Bombers")
    void opponentsCreatureEnters() {
        harness.addToBattlefield(player1, new MoggBombers());
        Permanent entering = harness.enterBattlefieldAndReturn(player2, new MoggFlunkies());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(player1.getId(), player2.getId())
                .doesNotContain(entering.getId());

        int lifeBefore = gd.getLife(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        harness.assertInGraveyard(player1, "Mogg Bombers");
        harness.assertOnBattlefield(player2, "Mogg Flunkies");
    }

    @Test
    @DisplayName("Mogg Bombers can deal its damage to a planeswalker")
    void canTargetPlaneswalker() {
        harness.addToBattlefield(player1, new MoggBombers());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new MoggFlunkies());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(player1.getId(), player2.getId(), planeswalker.getId())
                .doesNotContain(entering.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mogg Bombers");
        harness.assertOnBattlefield(player1, "Mogg Flunkies");
    }
}
