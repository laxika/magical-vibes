package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrRsMap.class, Forest.class})
class ThrRsMapTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldSearchesForABasicLand() {
        harness.setHand(player1, List.of(new ThrRsMap()));
        harness.setLibrary(player1, List.of(new Forest(), new ThrRsMap()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).singleElement().isInstanceOf(Forest.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void activatingTheAbilityDrawsThenDiscards() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new ThrRsMap());
        harness.setHand(player1, List.of(new ThrRsMap()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(map.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().isInstanceOf(Forest.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringTheBattlefieldDoesNotOfferNonbasicCards() {
        harness.setHand(player1, List.of(new ThrRsMap()));
        harness.setLibrary(player1, List.of(new ThrRsMap()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenABasicLandIsPresent() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ThrRsMap()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDiscardTheCardJustDrawn() {
        harness.addToBattlefield(player1, new ThrRsMap());
        ThrRsMap heldCard = new ThrRsMap();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(heldCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
