package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtractAConfession.class, GrizzlyBears.class, HillGiant.class, SerraAngel.class})
class ExtractAConfessionTest extends BaseCardTest {

    @Test
    void withoutEvidenceOpponentChoosesCreatureToSacrifice() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent serraAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castExtractAConfession();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(hillGiant.getId(), serraAngel.getId());
        harness.handlePermanentChosen(player2, hillGiant.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void withEvidenceOpponentSacrificesCreatureWithGreatestPower() {
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        Card thirdEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence, thirdEvidence));
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());
        castExtractAConfessionWithEvidence();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Extract a Confession");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstEvidence, secondEvidence, thirdEvidence);
    }

    private void castExtractAConfession() {
        harness.setHand(player1, List.of(new ExtractAConfession()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void castExtractAConfessionWithEvidence() {
        harness.setHand(player1, List.of(new ExtractAConfession()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1, 2));
    }
}
