package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.cards.r.Recoup;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tombfire.class, CoffinPurge.class, AvenFisher.class, Recoup.class})
class TombfireTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all cards with flashback from the target player's graveyard")
    void exilesAllCardsWithFlashback() {
        Card flashbackCard = new CoffinPurge();
        Card ordinaryCard = new AvenFisher();
        harness.setGraveyard(player2, List.of(flashbackCard, ordinaryCard));
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(flashbackCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(ordinaryCard.getId());
    }

    @Test
    @DisplayName("Leaves cards without flashback in the target player's graveyard")
    void leavesCardsWithoutFlashback() {
        Card ordinaryCard = new AvenFisher();
        harness.setGraveyard(player2, List.of(ordinaryCard));
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(ordinaryCard.getId());
    }

    @Test
    @DisplayName("Exiles every matching card only from the targeted player's graveyard")
    void exilesEveryMatchingCardOnlyFromTargetedGraveyard() {
        Card firstFlashbackCard = new CoffinPurge();
        Card secondFlashbackCard = new CoffinPurge();
        Card ordinaryCard = new AvenFisher();
        Card flashbackCardInUntargetedGraveyard = new CoffinPurge();
        harness.setGraveyard(player1, List.of(flashbackCardInUntargetedGraveyard));
        harness.setGraveyard(player2, List.of(firstFlashbackCard, ordinaryCard, secondFlashbackCard));
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(firstFlashbackCard.getId(), secondFlashbackCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(ordinaryCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(flashbackCardInUntargetedGraveyard.getId());
    }
    @Test
    @DisplayName("Can target a player with an empty graveyard")
    void resolvesWithEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tombfire");
    }

    @Test
    @DisplayName("Exiles cards that gained flashback from Recoup when targeting yourself")
    void exilesCardsWithGrantedFlashback() {
        Card grantedFlashbackCard = new Tombfire();
        Card ordinaryCard = new AvenFisher();
        harness.setGraveyard(player1, List.of(grantedFlashbackCard, ordinaryCard));
        harness.setHand(player1, List.of(new Recoup(), new Tombfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, grantedFlashbackCard.getId());
        Card recoup = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Recoup)
                .findFirst().orElseThrow();
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(grantedFlashbackCard.getId(), recoup.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(grantedFlashbackCard.getId(), recoup.getId())
                .contains(ordinaryCard.getId());
    }
}
