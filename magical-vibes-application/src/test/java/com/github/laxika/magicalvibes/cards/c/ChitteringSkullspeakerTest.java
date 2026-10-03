package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChitteringSkullspeaker.class, Forest.class})
class ChitteringSkullspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, intensifies, draws one card, and loses one life")
    void entersAndUsesInitialIntensity() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ChitteringSkullspeaker()));
        harness.setLife(player1, 20);
        castSkullspeaker();

        Permanent skullspeaker = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gd.getCardIntensity(skullspeaker.getCard().getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each new copy intensifies all owned copies and uses its new intensity")
    void intensifiesAllOwnedCopies() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ChitteringSkullspeaker()));
        harness.setLife(player1, 20);
        castSkullspeaker();

        Permanent first = gd.playerBattlefields.get(player1.getId()).get(0);
        harness.setHand(player1, List.of(new ChitteringSkullspeaker()));
        castSkullspeaker();

        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(gd.getCardIntensity(first.getCard().getId())).isEqualTo(2);
        assertThat(gd.getCardIntensity(second.getCard().getId())).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A copy already in hand retains its intensity and draws two on entering")
    void previouslyIntensifiedCopyUsesAccumulatedIntensity() {
        ChitteringSkullspeaker first = new ChitteringSkullspeaker();
        ChitteringSkullspeaker second = new ChitteringSkullspeaker();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        castSkullspeaker();
        assertThat(gd.getCardIntensity(second)).isEqualTo(1);
        castSkullspeaker();

        assertThat(gd.getCardIntensity(first)).isEqualTo(2);
        assertThat(gd.getCardIntensity(second)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Intensifies owned copies in other zones without affecting an opponent's copies")
    void intensifiesOwnedCopiesAcrossZones() {
        ChitteringSkullspeaker libraryCopy = new ChitteringSkullspeaker();
        ChitteringSkullspeaker graveyardCopy = new ChitteringSkullspeaker();
        ChitteringSkullspeaker exiledCopy = new ChitteringSkullspeaker();
        ChitteringSkullspeaker opponentHandCopy = new ChitteringSkullspeaker();
        ChitteringSkullspeaker opponentBattlefieldCopy = new ChitteringSkullspeaker();
        harness.setLibrary(player1, List.of(libraryCopy, new Forest()));
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setExile(player1, List.of(exiledCopy));
        harness.setHand(player1, List.of(new ChitteringSkullspeaker()));
        harness.setHand(player2, List.of(opponentHandCopy));
        harness.addToBattlefield(player2, opponentBattlefieldCopy);

        castSkullspeaker();

        assertThat(gd.getCardIntensity(libraryCopy)).isEqualTo(1);
        assertThat(gd.getCardIntensity(graveyardCopy)).isEqualTo(1);
        assertThat(gd.getCardIntensity(exiledCopy)).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponentHandCopy)).isZero();
        assertThat(gd.getCardIntensity(opponentBattlefieldCopy)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCopy);
        harness.assertLife(player2, 20);
    }

    private void castSkullspeaker() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
