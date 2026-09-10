package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingPhone.class, WrathOfGod.class, GrizzlyBears.class, HillGiant.class,
        Plains.class, Shock.class})
class LivingPhoneTest extends BaseCardTest {

    @Test
    @DisplayName("When Living Phone dies, it offers a creature with power 2 or less from the top five")
    void deathTriggerOffersSmallCreature() {
        harness.addToBattlefield(player1, new LivingPhone());
        Card smallCreature = new GrizzlyBears();
        Card powerTwoCreature = new GrizzlyBears();
        List<Card> topCards = List.of(smallCreature, new HillGiant(), new Shock(), powerTwoCreature,
                new Plains());
        destroyLivingPhone(topCards);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                smallCreature.getId(), powerTwoCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(smallCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(smallCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                topCards.get(1), topCards.get(2), topCards.get(3), topCards.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When no creature with power 2 or less is revealed, all five cards go to the bottom")
    void noSmallCreatureGoesToHand() {
        harness.addToBattlefield(player1, new LivingPhone());
        List<Card> topCards = List.of(new HillGiant(), new Shock(), new Plains(), new Shock(), new Plains());
        destroyLivingPhone(topCards);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void destroyLivingPhone(List<Card> topCards) {
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
