package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostLegion.class})
class LostLegionTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldOffersScryTwo() {
        castLostLegion();
        resolveLostLegionAndEtb();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    void scryTwoCanReorderTheTopOfTheLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);
        Card secondCard = deck.get(1);

        castLostLegion();
        resolveLostLegionAndEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck).containsSubsequence(secondCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castLostLegion() {
        harness.setHand(player1, List.of(new LostLegion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void resolveLostLegionAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
