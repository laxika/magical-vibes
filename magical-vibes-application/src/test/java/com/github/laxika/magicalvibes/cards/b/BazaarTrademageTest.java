package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BazaarTrademage.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class BazaarTrademageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws two cards before the controller discards three")
    void etbDrawsTwoThenDiscardsThree() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new BazaarTrademage(), new Forest(), new Island(), new Mountain())));
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Island", "Mountain", "Plains", "Swamp");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains", "Swamp");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Island", "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Bazaar Trademage");
    }

    @Test
    @DisplayName("With fewer than three cards after drawing, discard the entire hand")
    void discardsEntireHandWhenOnlyTwoCardsAreAvailable() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new Forest()));
        harness.castFromHand(player1, new BazaarTrademage(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains", "Swamp");
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        harness.assertOnBattlefield(player1, "Bazaar Trademage");
    }

    @Test
    @DisplayName("The second player's trigger draws and discards only for its controller")
    void secondPlayerDrawsAndDiscardsWithoutAffectingOpponent() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Plains(), new Swamp(), new Forest()));
        harness.castFromHand(player2, new BazaarTrademage(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Plains", "Swamp");
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Swamp");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Bazaar Trademage");
    }
}
