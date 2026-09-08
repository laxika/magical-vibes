package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EncroachingMycosynthTest extends BaseCardTest {

    private static Card card(CardType type) {
        Card card = new Card();
        card.setName(type.name());
        card.setType(type);
        card.setManaCost("{1}");
        return card;
    }

    @Test
    void ownNonlandPermanentsBecomeArtifacts() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, card(CardType.ENCHANTMENT));
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, card(CardType.LAND));
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, card(CardType.CREATURE));
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        assertThat(gqs.isArtifact(gd, ownPermanent)).isTrue();
        assertThat(gqs.isArtifact(gd, ownLand)).isFalse();
        assertThat(gqs.isArtifact(gd, opponentPermanent)).isFalse();
    }

    @Test
    void ownNonlandPermanentCardsBecomeArtifactsOutsideBattlefield() {
        Card ownCard = card(CardType.ENCHANTMENT);
        Card ownLand = card(CardType.LAND);
        Card opponentCard = card(CardType.CREATURE);
        harness.setHand(player1, List.of(ownCard, ownLand));
        harness.setHand(player2, List.of(opponentCard));
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        assertThat(gqs.cardHasType(ownCard, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(ownLand, CardType.ARTIFACT, gd, player1.getId())).isFalse();
        assertThat(gqs.cardHasType(opponentCard, CardType.ARTIFACT, gd, player2.getId())).isFalse();
    }
}
