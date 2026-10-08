package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BashfulBeastie;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValgavothsOnslaught.class, BashfulBeastie.class, Forest.class, Mountain.class})
class ValgavothsOnslaughtTest extends BaseCardTest {

    @Test
    void manifestsDreadTwiceThenPutsTwoCountersOnEachManifestedCreature() {
        Card firstCreature = new BashfulBeastie();
        Card firstGraveyardCard = new Forest();
        Card secondCreature = new BashfulBeastie();
        Card secondGraveyardCard = new Mountain();
        prepareSpell(List.of(firstCreature, firstGraveyardCard, secondCreature, secondGraveyardCard));

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(secondCreature.getId()));

        List<Permanent> manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .toList();
        assertThat(manifested).hasSize(2);
        assertThat(manifested)
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstGraveyardCard, secondGraveyardCard);
    }

    @Test
    void stopsWhenTheLibraryRunsOut() {
        Card manifestedCard = new BashfulBeastie();
        prepareSpell(List.of(manifestedCard));

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        List<Permanent> manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .toList();
        assertThat(manifested).hasSize(1);
        assertThat(manifested.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroXDoesNotLookAtCardsOrPutCountersOnExistingCreatures() {
        Card libraryCard = new Forest();
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        prepareSpell(List.of(libraryCard));

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(existing);
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersAreAddedOnlyAfterAllManifestationsAndOnlyToCreaturesCreatedThisWay() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BashfulBeastie());
        existing.setManifested(true);
        existing.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Card first = new Forest();
        Card discardedFirst = new Mountain();
        Card second = new Mountain();
        Card discardedSecond = new Forest();
        prepareSpell(List.of(first, discardedFirst, second, discardedSecond));

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        Permanent firstManifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == first).findFirst().orElseThrow();
        assertThat(firstManifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != existing))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.isManifested()).isTrue();
                    assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
                });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(discardedFirst, discardedSecond)
                .doesNotContain(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anEmptyLibraryCompletesWithoutCreatingCreaturesOrRequestingChoices() {
        prepareSpell(List.of());

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersRemainWhenAManifestedCreatureIsTurnedFaceUpForItsManaCost() {
        Card creature = new BashfulBeastie();
        prepareSpell(List.of(creature, new Forest()));
        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void prepareSpell(List<Card> library) {
        harness.setHand(player1, List.of(new ValgavothsOnslaught()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
