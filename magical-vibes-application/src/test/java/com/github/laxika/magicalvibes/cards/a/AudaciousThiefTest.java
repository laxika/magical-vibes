package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AudaciousThief.class, Murder.class})
class AudaciousThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws a card and makes the controller lose 1 life")
    void attackingDrawsAndLosesLife() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AudaciousThief()));
        harness.setLife(player1, 20);
        addCreatureReady(player1, new AudaciousThief());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each attacking Thief triggers once, while a nonattacking Thief does not")
    void onlyAttackingThievesTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AudaciousThief(), new AudaciousThief(), new AudaciousThief()));
        harness.setLife(player1, 20);
        addCreatureReady(player1, new AudaciousThief());
        addCreatureReady(player1, new AudaciousThief());
        addCreatureReady(player1, new AudaciousThief());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The attacking Thief's controller draws and loses life even when player two attacks")
    void opponentControlledThiefBenefitsOpponent() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new AudaciousThief()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new AudaciousThief());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing the Thief in response does not stop its attack trigger")
    void attackTriggerResolvesAfterSourceIsDestroyed() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Murder()));
        harness.setLibrary(player1, List.of(new AudaciousThief()));
        harness.setLife(player1, 20);
        var thief = addCreatureReady(player1, new AudaciousThief());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, thief.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Audacious Thief");
        harness.assertInGraveyard(player1, "Audacious Thief");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }
}
