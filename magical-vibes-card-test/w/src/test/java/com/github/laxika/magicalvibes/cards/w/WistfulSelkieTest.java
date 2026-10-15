package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WistfulSelkie.class, Forest.class})
class WistfulSelkieTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new WistfulSelkie()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wistful Selkie");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wistful Selkie");
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new WistfulSelkie()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Entering without being cast draws only for the entering creature's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        WistfulSelkie drawnCard = new WistfulSelkie();
        WistfulSelkie nextCard = new WistfulSelkie();
        harness.setLibrary(player2, List.of(drawnCard, nextCard));

        harness.enterBattlefieldAndReturn(player2, new WistfulSelkie());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB draw resolves even after Selkie dies")
    void etbDrawResolvesAfterSourceDies() {
        harness.setHand(player1, List.of(new WistfulSelkie()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        WistfulSelkie drawnCard = new WistfulSelkie();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Wistful Selkie").setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Wistful Selkie");
        harness.assertInGraveyard(player1, "Wistful Selkie");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
