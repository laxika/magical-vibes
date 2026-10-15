package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionSkeins.class, MistralCharger.class})
class VisionSkeinsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws two cards")
    void eachPlayerDrawsTwoCards() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MistralCharger(), new MistralCharger()));
        harness.setLibrary(player2, List.of(new MistralCharger(), new MistralCharger()));

        harness.castFromHand(player1, new VisionSkeins(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent casting the spell still makes both players draw two")
    void opponentCastingMakesBothPlayersDraw() {
        MistralCharger first = new MistralCharger();
        MistralCharger second = new MistralCharger();
        MistralCharger third = new MistralCharger();
        MistralCharger fourth = new MistralCharger();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(third, fourth));

        harness.castFromHand(player2, new VisionSkeins(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(third, fourth);
        harness.assertInGraveyard(player2, "Vision Skeins");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Both players lose together when both libraries have fewer than two cards")
    void insufficientLibrariesEndInADraw() {
        MistralCharger first = new MistralCharger();
        MistralCharger second = new MistralCharger();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(first));
        harness.setLibrary(player2, List.of(second));

        harness.castFromHand(player1, new VisionSkeins(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Opponent completes both draws before the caster loses for an empty library")
    void opponentDrawsBeforeCasterLoses() {
        MistralCharger first = new MistralCharger();
        MistralCharger second = new MistralCharger();
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(first, second));

        harness.castFromHand(player1, new VisionSkeins(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
