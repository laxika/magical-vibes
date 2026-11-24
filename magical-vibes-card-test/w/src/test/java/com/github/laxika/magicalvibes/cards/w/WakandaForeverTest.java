package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
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

        PendingInteraction.MultiGraveyardChoice firstPick = activeChoice();
        assertThat(firstPick.cards()).containsExactly(battlefieldCard, handCard);
        // The six cards are revealed, not moved, until the picks are made.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(revealed);

        chooseCard(battlefieldCard);

        PendingInteraction.MultiGraveyardChoice secondPick = activeChoice();
        assertThat(secondPick.prompt()).contains("into your hand");
        assertThat(secondPick.cards()).containsExactly(handCard);
        chooseCard(handCard);

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

        activeChoice();
        chooseCard(null);

        PendingInteraction.MultiGraveyardChoice secondPick = activeChoice();
        assertThat(secondPick.prompt()).contains("into your hand");
        assertThat(secondPick.cards()).containsExactly(firstPermanent, secondPermanent);
        chooseCard(secondPermanent);

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

        activeChoice();
        chooseCard(null);
        activeChoice();
        chooseCard(null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Wakanda Forever!", "Grizzly Bears", "Forest", "Shock", "Shock", "Shock", "Shock");
    }

    @Test
    void mayTakeBattlefieldPermanentAndDeclineHandPickWithoutTouchingSeventhCard() {
        Card battlefieldCard = new Forest();
        Card unchosenPermanent = new GrizzlyBears();
        Card seventhCard = new Forest();
        castAndResolve(List.of(battlefieldCard, unchosenPermanent, new Shock(),
                new Shock(), new Shock(), new Shock(), seventhCard));

        activeChoice();
        chooseCard(battlefieldCard);
        activeChoice();
        chooseCard(null);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(entered.getCard()).isSameAs(battlefieldCard);
        assertThat(entered.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(entered.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(seventhCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosenPermanent);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void handlesFewerThanSixCardsAndSkipsHandPickWhenNoPermanentRemains() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        castAndResolve(List.of(creature, instant));

        activeChoice();
        chooseCard(creature);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, entered.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(entered);
    }

    @Test
    void putsAllNonpermanentsIntoGraveyardWithoutOfferingChoices() {
        List<Card> revealed = List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        castAndResolve(revealed);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithEmptyLibraryWithoutOfferingChoices() {
        castAndResolve(List.of());

        harness.assertInGraveyard(player1, "Wakanda Forever!");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WakandaForever()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.MultiGraveyardChoice activeChoice() {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        return choice;
    }

    private void chooseCard(Card card) {
        harness.handleMultipleCardsChosen(player1, card == null ? List.of() : List.of(card.getId()));
    }
}
