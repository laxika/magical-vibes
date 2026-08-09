package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaosWandTest extends BaseCardTest {

    @Test
    @DisplayName("Offers the first instant or sorcery after exiling cards from an opponent's library")
    void offersFirstInstantOrSorcery() {
        activateWithLibrary(List.of(new Forest(), new Divination(), new Insight()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Divination");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Insight");
    }

    @Test
    @DisplayName("Casts the found spell for free under the activating player's control")
    void castsFoundSpellWithoutPaying() {
        activateWithLibrary(List.of(new Forest(), new Divination()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination")
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getControllerId().equals(player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bottoms the exiled cards when the activating player declines")
    void declineBottomsExiledCards() {
        activateWithLibrary(List.of(new Forest(), new Divination()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Divination"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Divination");
    }

    @Test
    @DisplayName("Returns the entire library to the bottom when no instant or sorcery is found")
    void noInstantOrSorceryReturnsAllCards() {
        activateWithLibrary(List.of(new Forest(), new Insight()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Insight");
    }

    @Test
    @DisplayName("Cannot target the activating player")
    void cannotTargetSelf() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ChaosWand()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateWithLibrary(List<Card> library) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(library);
        harness.setHand(player1, List.of(new ChaosWand()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
    }
}
