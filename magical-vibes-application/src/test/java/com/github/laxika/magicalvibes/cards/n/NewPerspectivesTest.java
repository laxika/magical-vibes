package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NewPerspectivesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws three cards")
    void entersDrawsThreeCards() {
        harness.setHand(player1, List.of(new NewPerspectives()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new LlanowarElves()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "New Perspectives");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cycling costs {0} with seven or more cards in hand")
    void cyclingIsFreeWithSevenCardsInHand() {
        harness.addToBattlefield(player1, new NewPerspectives());
        harness.setHand(player1, sevenCardHandWithCensor());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // No mana in the pool — the {U} cycling cost must be replaced with {0}.

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling still costs mana with fewer than seven cards in hand")
    void cyclingCostsManaWithFewerThanSevenCards() {
        harness.addToBattlefield(player1, new NewPerspectives());
        List<Card> hand = new ArrayList<>();
        hand.add(new Censor());
        for (int i = 0; i < 5; i++) { // six cards total — below the seven threshold
            hand.add(new LlanowarElves());
        }
        harness.setHand(player1, hand);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // No mana in the pool: the replacement does not apply, so {U} is unaffordable.

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Censor");
    }

    @Test
    @DisplayName("Cycling still costs mana without New Perspectives on the battlefield")
    void cyclingCostsManaWithoutNewPerspectives() {
        harness.setHand(player1, sevenCardHandWithCensor());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // Seven cards in hand but no New Perspectives, no mana: {U} is unaffordable.

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Censor");
    }

    private List<Card> sevenCardHandWithCensor() {
        List<Card> hand = new ArrayList<>();
        hand.add(new Censor()); // index 0, the card to cycle
        for (int i = 0; i < 6; i++) {
            hand.add(new LlanowarElves());
        }
        return hand;
    }
}
