package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourPlansMeanNothing.class, Forest.class, GrizzlyBears.class})
class YourPlansMeanNothingTest extends BaseCardTest {

    @Test
    void targetedControllerDrawsSevenAndTargetedOpponentDrawsOneFewerThanDiscarded() {
        List<Card> controllerLibrary = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        List<Card> opponentLibrary = List.of(new Forest(), new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, controllerLibrary);
        harness.setLibrary(player2, opponentLibrary);

        resolveScheme(List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }

    @Test
    void untargetedPlayersDoNotDrawAndOneDiscardedCardProducesNoDraw() {
        Card controllerHand = new GrizzlyBears();
        Card opponentHand = new GrizzlyBears();
        Card controllerLibraryCard = new Forest();
        Card opponentLibraryCard = new Forest();
        harness.setHand(player1, List.of(controllerHand));
        harness.setHand(player2, List.of(opponentHand));
        harness.setLibrary(player1, List.of(controllerLibraryCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        resolveScheme(List.of(player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }

    @Test
    void mayResolveWithNoTargets() {
        Card controllerHand = new GrizzlyBears();
        Card opponentHand = new GrizzlyBears();
        harness.setHand(player1, List.of(controllerHand));
        harness.setHand(player2, List.of(opponentHand));

        resolveScheme(List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHand);
    }

    private void resolveScheme(List<java.util.UUID> targetIds) {
        YourPlansMeanNothing scheme = new YourPlansMeanNothing();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                null,
                targetIds));
        harness.passBothPriorities();
    }
}
