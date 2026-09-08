package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValgavothsOnslaught.class, GrizzlyBears.class, Forest.class, Mountain.class})
class ValgavothsOnslaughtTest extends BaseCardTest {

    @Test
    void manifestsDreadTwiceThenPutsTwoCountersOnEachManifestedCreature() {
        Card firstCreature = new GrizzlyBears();
        Card firstGraveyardCard = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondGraveyardCard = new Mountain();
        prepareSpell(List.of(firstCreature, firstGraveyardCard, secondCreature, secondGraveyardCard));

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

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
        Card manifestedCard = new GrizzlyBears();
        prepareSpell(List.of(manifestedCard));

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        List<Permanent> manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .toList();
        assertThat(manifested).hasSize(1);
        assertThat(manifested.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void prepareSpell(List<Card> library) {
        harness.setHand(player1, List.of(new ValgavothsOnslaught()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
