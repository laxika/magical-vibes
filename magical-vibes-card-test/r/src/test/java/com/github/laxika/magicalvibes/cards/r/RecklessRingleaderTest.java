package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessRingleader.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class RecklessRingleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing among the creature cards in hand gives the chosen one haste, which it has once cast")
    void chosenCreatureCardPerpetuallyGainsHaste() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Mountain mountain = new Mountain();
        castRingleader(List.of(bears, giant, mountain));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                .extracting(Card::getId).containsExactly(bears.getId(), giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent castBears = findPermanent(player1, "Grizzly Bears");
        Permanent castGiant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.hasKeyword(gd, castBears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, castGiant, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A lone creature card in hand is chosen without a prompt")
    void loneCreatureCardIsChosenUnasked() {
        GrizzlyBears bears = new GrizzlyBears();
        castRingleader(List.of(bears, new Mountain()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.perpetualCardKeywords.get(bears.getId())).containsExactly(Keyword.HASTE);
    }

    @Test
    @DisplayName("With no creature card in hand nothing is chosen and nothing gains haste")
    void noCreatureCardMeansNoChoice() {
        Mountain mountain = new Mountain();
        castRingleader(List.of(mountain));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.perpetualCardKeywords).doesNotContainKey(mountain.getId());
    }

    @Test
    @DisplayName("Only its controller's hand is chosen from")
    void opponentsCreatureCardsAreNotChosen() {
        GrizzlyBears opposing = new GrizzlyBears();
        harness.setHand(player2, List.of(opposing));
        castRingleader(List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.perpetualCardKeywords).doesNotContainKey(opposing.getId());
    }

    @Test
    @DisplayName("The log names no card of the hand")
    void logNamesNoHandCard() {
        castRingleader(List.of(new GrizzlyBears()));

        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().equals("Each affected card in "
                + gd.playerIdToName.get(player1.getId()) + "'s hand perpetually gains haste."));
    }

    private void castRingleader(List<Card> rest) {
        List<Card> hand = new java.util.ArrayList<>();
        hand.add(new RecklessRingleader());
        hand.addAll(rest);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
