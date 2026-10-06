package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shroudstomper.class, GrizzlyBears.class})
class ShroudstomperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering makes each opponent lose 2 life, gains 2 life, and draws a card")
    void enterAbility() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new Shroudstomper());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Attacking makes each opponent lose 2 life, gains 2 life, and draws a card")
    void attackAbility() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        addCreatureReady(player1, new Shroudstomper());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Entering under the other player's control benefits that player")
    void enterAbilityForOtherController() {
        Shroudstomper drawnCard = new Shroudstomper();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new Shroudstomper());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("An enter trigger still resolves after its source dies")
    void enterAbilityResolvesAfterSourceDies() {
        Shroudstomper drawnCard = new Shroudstomper();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        var source = harness.enterBattlefieldAndReturn(player1, new Shroudstomper());
        source.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Shroudstomper");
        harness.assertInGraveyard(player1, "Shroudstomper");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
