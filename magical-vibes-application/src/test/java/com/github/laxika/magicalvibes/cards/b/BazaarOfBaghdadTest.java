package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BazaarOfBaghdad.class, Forest.class, GrizzlyBears.class})
class BazaarOfBaghdadTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards three cards")
    void drawsTwoThenDiscardsThree() {
        Permanent bazaar = addReadyBazaar();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(bazaar.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyBazaar() {
        Permanent bazaar = harness.addToBattlefieldAndReturn(player1, new BazaarOfBaghdad());
        bazaar.setSummoningSick(false);
        return bazaar;
    }

    @Test
    @DisplayName("Can discard the newly drawn cards")
    void canDiscardNewlyDrawnCards() {
        addReadyBazaar();
        Card kept = new Forest();
        Card discarded = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of(kept, discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(kept, discarded, firstDraw, secondDraw);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, discarded);
    }

    @Test
    @DisplayName("An empty initial hand discards both drawn cards and finishes resolving")
    void discardsAsManyAsPossible() {
        addReadyBazaar();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered land can activate and its ability uses the stack")
    void newlyEnteredLandCanActivate() {
        Permanent bazaar = harness.addToBattlefieldAndReturn(player1, new BazaarOfBaghdad());
        bazaar.setSummoningSick(true);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(bazaar.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
