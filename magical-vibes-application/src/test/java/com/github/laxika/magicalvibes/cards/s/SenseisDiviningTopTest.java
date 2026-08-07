package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SenseisDiviningTopTest extends BaseCardTest {

    @Test
    @DisplayName("First ability reorders the top three cards of the library")
    void reordersTopThree() {
        harness.addToBattlefield(player1, new SenseisDiviningTop());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(deck.get(0)).isSameAs(top2);
        assertThat(deck.get(1)).isSameAs(top1);
        assertThat(deck.get(2)).isSameAs(top0);
    }

    @Test
    @DisplayName("First ability leaves the top card intact and does not tap the artifact")
    void firstAbilityDoesNotTap() {
        Permanent top = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(top.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability draws a card and puts the artifact on top of its owner's library")
    void drawsAndTucksItself() {
        Permanent top = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card drawn = deck.getFirst();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(top);
        assertThat(deck.getFirst().getName()).isEqualTo("Sensei's Divining Top");
    }
}
