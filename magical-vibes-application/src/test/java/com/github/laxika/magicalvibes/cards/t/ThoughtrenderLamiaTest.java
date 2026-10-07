package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FontOfVigor;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtrenderLamia.class, FontOfVigor.class, GoldenHind.class})
class ThoughtrenderLamiaTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent discards a card when Thoughtrender Lamia enters")
    void ownEntryTriggers() {
        harness.setHand(player2, List.of(new GoldenHind(), new GoldenHind()));
        harness.castFromHand(player1, new ThoughtrenderLamia(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each opponent discards a card when another enchantment enters under your control")
    void anotherEnchantmentEntryTriggers() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player2, List.of(new GoldenHind(), new GoldenHind()));
        harness.castFromHand(player1, new FontOfVigor(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment creature enters under your control")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player2, List.of(new GoldenHind(), new GoldenHind()));
        harness.castFromHand(player1, new GoldenHind(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's enchantment enters")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player1, List.of(new GoldenHind()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new FontOfVigor(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent the trigger from resolving")
    void emptyOpponentHand() {
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new ThoughtrenderLamia(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thoughtrender Lamia");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both Lamias trigger once when a second Lamia enters")
    void anotherLamiaTriggersBothConstellationAbilities() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player2, List.of(new GoldenHind(), new GoldenHind(), new GoldenHind()));
        harness.castFromHand(player1, new ThoughtrenderLamia(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses the discarded card and the controller keeps their hand")
    void opponentChoosesDiscard() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player2, List.of(new GoldenHind(), new ThoughtrenderLamia()));
        harness.castFromHand(player1, new FontOfVigor(), "{1}{W}");
        harness.setHand(player1, List.of(new GoldenHind()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Thoughtrender Lamia");
        harness.assertInHand(player2, "Golden Hind");
        harness.assertInHand(player1, "Golden Hind");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
