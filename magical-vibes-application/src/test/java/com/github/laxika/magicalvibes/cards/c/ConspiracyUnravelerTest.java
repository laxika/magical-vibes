package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConspiracyUnraveler.class, GrizzlyBears.class})
class ConspiracyUnravelerTest extends BaseCardTest {

    @Test
    void mayCollectEvidenceInsteadOfPayingManaForCreatureSpells() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        Card thirdEvidence = new GrizzlyBears();
        Card fourthEvidence = new GrizzlyBears();
        Card fifthEvidence = new GrizzlyBears();
        List<Card> evidence = List.of(firstEvidence, secondEvidence, thirdEvidence,
                fourthEvidence, fifthEvidence);

        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        harness.setGraveyard(player1, evidence);
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));

        assertThat(gd.stack.getLast().isAlternateCost()).isTrue();
        assertThat(gd.stack.getLast().isCollectEvidenceCostPaid()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }
}
