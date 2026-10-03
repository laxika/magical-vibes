package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateBloodseeker.class})
class DesperateBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target opponent mills two cards")
    void etbMillsTargetOpponent() {
        harness.setLibrary(player2, List.of(new DesperateBloodseeker(), new DesperateBloodseeker(),
                new DesperateBloodseeker()));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void etbMillsController() {
        harness.setLibrary(player1, List.of(new DesperateBloodseeker(), new DesperateBloodseeker()));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void millsOnlyTheTopTwoCardsOfTheTargetLibrary() {
        DesperateBloodseeker first = new DesperateBloodseeker();
        DesperateBloodseeker second = new DesperateBloodseeker();
        DesperateBloodseeker third = new DesperateBloodseeker();
        harness.setLibrary(player2, List.of(first, second, third));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void millsTheRemainingCardWhenLibraryHasFewerThanTwoCards() {
        DesperateBloodseeker remaining = new DesperateBloodseeker();
        harness.setLibrary(player2, List.of(remaining));
        castDesperateBloodseeker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    void canTargetAPlayerWithAnEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        castDesperateBloodseeker();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifelinkGainsLifeWhenDealingCombatDamage() {
        Permanent bloodseeker = harness.addToBattlefieldAndReturn(player1, new DesperateBloodseeker());
        bloodseeker.setSummoningSick(false);
        bloodseeker.setAttacking(true);
        bloodseeker.setAttackTarget(player2.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        harness.resolveCombatDamage();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    private void castDesperateBloodseeker() {
        harness.castFromHand(player1, new DesperateBloodseeker(), "{1}{B}");
    }
}
