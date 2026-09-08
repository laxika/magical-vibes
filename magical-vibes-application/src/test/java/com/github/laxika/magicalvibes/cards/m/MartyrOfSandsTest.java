package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrOfSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals two white cards, gains six life, and sacrifices itself")
    void revealsWhiteCardsAndGainsThreeTimesXLife() {
        Card firstWhiteCard = new HealingSalve();
        Card secondWhiteCard = new SwordsToPlowshares();
        harness.setHand(player1, List.of(firstWhiteCard, secondWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstWhiteCard.getId(), secondWhiteCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId(), secondWhiteCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstWhiteCard, secondWhiteCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
    }

    @Test
    @DisplayName("Cannot reveal more white cards than are in hand")
    void cannotRevealMoreWhiteCardsThanInHand() {
        harness.setHand(player1, List.of(new RagingGoblin()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
