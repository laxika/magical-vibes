package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.u.UrzasRage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideCrab.class, UrzasRage.class})
class RiptideCrabTest extends BaseCardTest {

    @Test
    @DisplayName("Riptide Crab dies from Urza's Rage, draws a card")
    void diesFromUrzasRageDrawsCard() {
        harness.addToBattlefield(player1, new RiptideCrab());

        UrzasRage drawnCard = new UrzasRage();
        harness.setHand(player1, List.of(new UrzasRage()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID riptideCrabId = harness.getPermanentId(player1, "Riptide Crab");

        harness.castInstant(player1, 0, riptideCrabId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Riptide Crab");
        harness.assertInGraveyard(player1, "Riptide Crab");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Riptide Crab's controller draws when an opponent destroys it")
    void controllerDrawsWhenOpponentDestroysIt() {
        harness.addToBattlefield(player2, new RiptideCrab());

        UrzasRage drawnCard = new UrzasRage();
        harness.setHand(player1, List.of(new UrzasRage()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID riptideCrabId = harness.getPermanentId(player2, "Riptide Crab");
        harness.castInstant(player1, 0, riptideCrabId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Riptide Crab");
        harness.assertInGraveyard(player2, "Riptide Crab");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }
}
