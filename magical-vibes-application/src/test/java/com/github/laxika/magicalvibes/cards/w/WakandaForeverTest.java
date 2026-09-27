package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakandaForever.class, Forest.class, GrizzlyBears.class, Shock.class})
class WakandaForeverTest extends BaseCardTest {

    @Test
    void putsOnePermanentOntoBattlefieldWithIndestructibleCounterAndAnotherIntoHand() {
        Card battlefieldCard = new GrizzlyBears();
        Card handCard = new Forest();
        List<Card> revealed = List.of(
                battlefieldCard, new Shock(), handCard, new Shock(), new Shock(), new Shock());
        castAndResolve(revealed);

        PendingInteraction.LibrarySearch firstPick = activeLibrarySearch();
        assertThat(firstPick.params().cards()).containsExactly(battlefieldCard, handCard);
        assertThat(firstPick.params().sourceCards()).containsExactlyElementsOf(revealed);

        chooseLibraryCard(firstPick, battlefieldCard);

        PendingInteraction.LibrarySearch secondPick = activeLibrarySearch();
        assertThat(secondPick.params().destination()).isEqualTo(com.github.laxika.magicalvibes.model.LibrarySearchDestination.HAND);
        assertThat(secondPick.params().cards()).containsExactly(handCard);
        chooseLibraryCard(secondPick, handCard);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == battlefieldCard)
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Wakanda Forever!", "Shock", "Shock", "Shock", "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineBattlefieldPickAndPutASeparatePermanentIntoHand() {
        Card firstPermanent = new GrizzlyBears();
        Card secondPermanent = new Forest();
        castAndResolve(List.of(firstPermanent, new Shock(), secondPermanent,
                new Shock(), new Shock(), new Shock()));

        PendingInteraction.LibrarySearch firstPick = activeLibrarySearch();
        chooseLibraryCard(firstPick, null);

        PendingInteraction.LibrarySearch secondPick = activeLibrarySearch();
        assertThat(secondPick.params().destination()).isEqualTo(com.github.laxika.magicalvibes.model.LibrarySearchDestination.HAND);
        assertThat(secondPick.params().cards()).containsExactly(firstPermanent, secondPermanent);
        chooseLibraryCard(secondPick, secondPermanent);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == firstPermanent || permanent.getCard() == secondPermanent);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Wakanda Forever!", "Grizzly Bears", "Shock", "Shock", "Shock", "Shock");
    }

    @Test
    void mayDeclineBothPicksAndPutAllRevealedCardsIntoGraveyard() {
        castAndResolve(List.of(new GrizzlyBears(), new Shock(), new Forest(),
                new Shock(), new Shock(), new Shock()));

        chooseLibraryCard(activeLibrarySearch(), null);
        chooseLibraryCard(activeLibrarySearch(), null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Wakanda Forever!", "Grizzly Bears", "Forest", "Shock", "Shock", "Shock", "Shock");
    }

    private void castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WakandaForever()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeLibrarySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseLibraryCard(PendingInteraction.LibrarySearch search, Card card) {
        int index = card == null ? -1 : search.params().cards().indexOf(card);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }
}
