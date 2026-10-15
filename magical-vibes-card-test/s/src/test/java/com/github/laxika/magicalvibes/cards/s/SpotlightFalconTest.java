package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpotlightFalcon.class, GrizzlyBears.class})
class SpotlightFalconTest extends BaseCardTest {

    @Test
    void doesNotConjureWithoutCollectingEvidence() {
        harness.setHand(player1, List.of(new SpotlightFalcon()));
        addManaForSpotlightFalcon();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void collectingEvidenceConjuresSpotlightFalconIntoHand() {
        List<Card> evidence = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new SpotlightFalcon()));
        addManaForSpotlightFalcon();

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Spotlight Falcon");
                    assertThat(card.isToken()).isFalse();
                });
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    private void addManaForSpotlightFalcon() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
