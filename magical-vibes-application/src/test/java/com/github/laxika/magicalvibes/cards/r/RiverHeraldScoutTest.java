package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RiverHeraldScout.class, Forest.class, GrizzlyBears.class})
class RiverHeraldScoutTest extends BaseCardTest {

    @Test
    void exploreLandPutsLandIntoHandWithoutCounter() {
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void exploreNonLandAddsCounterAndMayPutCardIntoGraveyard() {
        Card nonLand = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonLand);

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void decliningExploreGraveyardChoiceLeavesNonLandOnTop() {
        Card nonLand = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(nonLand);

        castRiverHeraldScout();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonLand.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void exploreWithEmptyLibraryDoesNothing() {
        gd.playerDecks.get(player1.getId()).clear();

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castRiverHeraldScout() {
        harness.setHand(player1, List.of(new RiverHeraldScout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
