package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShowAndTellTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesBeforeCardsEnterTogether() {
        harness.setHand(player1, List.of(new ShowAndTell(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        castShowAndTell();

        UUID bearsId = handCardId(player1, 0);
        UUID forestId = handCardId(player2, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bearsId));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(forestId));

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void decliningLeavesTheChosenCardInHandAndStillPlacesOtherChoice() {
        harness.setHand(player1, List.of(new ShowAndTell(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        castShowAndTell();

        UUID bearsId = handCardId(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bearsId));
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void nonPermanentCardsAreNotEligible() {
        harness.setHand(player1, List.of(new ShowAndTell(), new LightningBolt()));
        harness.setHand(player2, List.of());
        castShowAndTell();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInGraveyard(player1, "Show and Tell");
    }

    private void castShowAndTell() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private UUID handCardId(com.github.laxika.magicalvibes.model.Player player, int index) {
        return gd.playerHands.get(player.getId()).get(index).getId();
    }
}
