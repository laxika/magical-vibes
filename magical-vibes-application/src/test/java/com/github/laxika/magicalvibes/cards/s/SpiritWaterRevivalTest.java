package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WanderingMusicians;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritWaterRevival.class, WanderingMusicians.class})
class SpiritWaterRevivalTest extends BaseCardTest {

    @Test
    void drawsTwoCardsWithoutWaterbend() {
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        harness.setLibrary(player1, List.of(new WanderingMusicians(), new WanderingMusicians()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SpiritWaterRevival);
    }

    @Test
    void waterbendShufflesGraveyardDrawsSevenAndRemovesHandLimit() {
        List<Card> graveyard = List.of(
                new WanderingMusicians(), new WanderingMusicians(), new WanderingMusicians(), new WanderingMusicians(),
                new WanderingMusicians(), new WanderingMusicians(), new WanderingMusicians());
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, graveyard);
        List<Permanent> sources = List.of(
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()),
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()),
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()),
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()),
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()),
                harness.addToBattlefieldAndReturn(player1, new WanderingMusicians()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                sources.stream().map(Permanent::getId).toList(), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);
        harness.passBothPriorities();

        assertThat(sources).allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SpiritWaterRevival);
    }

    @Test
    void canDeclineWaterbendEvenWhenEnoughManaIsAvailable() {
        Card graveyardCard = new WanderingMusicians();
        List<Card> library = supportCards(9);
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 2));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library.subList(2, 9));
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SpiritWaterRevival);
    }

    @Test
    void paysWaterbendEntirelyWithManaAndDrawsSevenFromAnEmptyGraveyard() {
        List<Card> library = supportCards(7);
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        castWithWaterbend(List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId()).doesNotContain(player2.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SpiritWaterRevival);
    }

    @Test
    void combinesManaAndSummoningSickCreaturesWithoutAffectingOpponentsZones() {
        List<Card> library = supportCards(3);
        List<Card> graveyard = supportCards(4);
        List<Card> opponentLibrary = supportCards(2);
        Card opponentGraveyardCard = new WanderingMusicians();
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player2, opponentLibrary);
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        Permanent unused = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        castWithWaterbend(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(unused.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).containsAll(library).containsAll(graveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId()).doesNotContain(player2.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SpiritWaterRevival);
    }

    @Test
    void cannotTapAnAlreadyTappedCreatureToPayWaterbend() {
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        source.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> castWithWaterbend(List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
    }

    private List<Card> supportCards(int count) {
        return IntStream.range(0, count).mapToObj(i -> (Card) new WanderingMusicians()).toList();
    }

    private void castWithWaterbend(List<UUID> sourceIds) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                sourceIds, List.of(), false, null, null, List.of(), List.of(), null, null, true);
    }
}
