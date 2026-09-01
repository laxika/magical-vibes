package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheUnagiOfKyoshiIsland.class, GrizzlyBears.class, Shock.class})
class TheUnagiOfKyoshiIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when an opponent draws their second card of the turn")
    void drawsTwoOnOpponentsSecondDraw() {
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        draw(player2);
        draw(player2);
        assertThat(gd.stack).hasSize(1);

        resolveTopOfStack();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        draw(player2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward can be paid by tapping artifacts or creatures")
    void wardCanBePaidWithWaterbend() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unagi.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(Permanent::isTapped)
                .hasSize(4);
        assertThat(unagi.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward counters the spell when waterbend cannot be paid")
    void wardCountersWhenWaterbendCannotBePaid() {
        Permanent unagi = harness.addToBattlefieldAndReturn(player1, new TheUnagiOfKyoshiIsland());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, unagi.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(unagi.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not trigger on its controller's draw or an opponent's first draw")
    void doesNotTriggerOnControllerOrFirstOpponentDraw() {
        harness.addToBattlefield(player1, new TheUnagiOfKyoshiIsland());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player1);
        draw(player2);

        assertThat(gd.stack).isEmpty();
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
