package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RhysticStudy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchivalWhorl.class, RhysticStudy.class, GrizzlyBears.class})
class ArchivalWhorlTest extends BaseCardTest {

    @Test
    @DisplayName("Without the Gift, each player shuffles and draws seven")
    void withoutGiftResetsEachPlayer() {
        harness.setHand(player1, List.of(new ArchivalWhorl(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        setLibraries(20);

        cast(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(15);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(15);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player2, "Rhystic Study")).isZero();
    }

    @Test
    @DisplayName("Promised Gift conjures Rhystic Study for the opponent and spares their zones")
    void promisedGiftConjuresRhysticStudy() {
        harness.setHand(player1, List.of(new ArchivalWhorl(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        setLibraries(20);

        cast(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(15);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(20);
        assertThat(countPermanents(player2, "Rhystic Study")).isEqualTo(1);
        assertThat(findPermanent(player2, "Rhystic Study").getCard().isToken()).isFalse();
    }

    private void cast(boolean giftPromised) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorceryWithGift(player1, 0, List.of(), giftPromised);
        harness.passBothPriorities();
    }

    private void setLibraries(int size) {
        List<Card> library = new ArrayList<>();
        List<Card> opponentLibrary = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            library.add(new GrizzlyBears());
            opponentLibrary.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, opponentLibrary);
    }
}
