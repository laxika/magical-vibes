package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SteelshaperApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to hand and searches for an Equipment")
    void returnsItselfToHandAndSearchesForEquipment() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new SteelshaperApprentice());
        apprentice.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        LeoninScimitar scimitar = new LeoninScimitar();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(scimitar, bears);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInHand(player1, "Steelshaper Apprentice");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Leonin Scimitar");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Does not find non-Equipment cards")
    void doesNotFindNonEquipmentCards() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new SteelshaperApprentice());
        apprentice.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Steelshaper Apprentice");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
