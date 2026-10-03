package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreacherousTerrain.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class TreacherousTerrainTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent equal to that opponent's land count")
    void dealsDamageBasedOnEachOpponentsLandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        harness.castFromHand(player1, new TreacherousTerrain(), "{6}{R}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Counts lands at resolution")
    void countsLandsAtResolution() {
        harness.castFromHand(player1, new TreacherousTerrain(), "{6}{R}{G}");
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new TreacherousTerrain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Treacherous Terrain");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.hasType(CardType.LAND)
                && card.getSupertypes().contains(CardSupertype.BASIC));
    }
}
