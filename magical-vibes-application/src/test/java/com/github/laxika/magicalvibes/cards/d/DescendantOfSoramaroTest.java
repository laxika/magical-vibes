package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescendantOfSoramaroTest extends BaseCardTest {

    @Test
    void reordersAsManyCardsAsAreInHand() {
        Permanent descendant = harness.addToBattlefieldAndReturn(player1, new DescendantOfSoramaro());
        descendant.setSummoningSick(false);

        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new GrizzlyBears();
        Card handCard3 = new GrizzlyBears();
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card thirdCard = new Mountain();
        Card fourthCard = new Plains();
        harness.setHand(player1, List.of(handCard1, handCard2, handCard3));
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(topCard, secondCard, thirdCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(thirdCard, secondCard, topCard, fourthCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNothingWithNoCardsInHand() {
        Permanent descendant = harness.addToBattlefieldAndReturn(player1, new DescendantOfSoramaro());
        descendant.setSummoningSick(false);
        Card topCard = new Forest();
        Card secondCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
