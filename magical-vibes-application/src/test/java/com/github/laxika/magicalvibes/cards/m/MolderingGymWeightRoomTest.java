package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MolderingGymWeightRoom.class, Forest.class, GrizzlyBears.class})
class MolderingGymWeightRoomTest extends BaseCardTest {

    @Test
    void unlockingMolderingGymPutsABasicLandOntoTheBattlefieldTapped() {
        Forest forest = new Forest();
        GrizzlyBears nonLand = new GrizzlyBears();
        harness.setHand(player1, List.of(new MolderingGymWeightRoom()));
        harness.setLibrary(player1, List.of(forest, nonLand));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        Permanent searchedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(searchedForest.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
    }

    @Test
    void unlockingWeightRoomManifestsDreadAndPutsThreeCountersOnThatCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new MolderingGymWeightRoom()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }
}
