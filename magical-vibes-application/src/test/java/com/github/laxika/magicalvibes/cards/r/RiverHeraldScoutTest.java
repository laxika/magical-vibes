package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BrackishBlunder;
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

@CardUsed({RiverHeraldScout.class, Forest.class, BrackishBlunder.class})
class RiverHeraldScoutTest extends BaseCardTest {

    @Test
    void exploreLandPutsLandIntoHandWithoutCounter() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void exploreNonLandAddsCounterAndMayPutCardIntoGraveyard() {
        Card nonLand = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(nonLand));

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void decliningExploreGraveyardChoiceLeavesNonLandOnTop() {
        Card nonLand = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(nonLand));

        castRiverHeraldScout();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(nonLand.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void exploreWithEmptyLibraryAddsCounter() {
        harness.setLibrary(player1, List.of());

        castRiverHeraldScout();

        Permanent scout = findPermanent(player1, "River Herald Scout");
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void exploreStillPutsLandIntoHandAfterScoutLeavesBattlefield() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        castScoutWithoutResolvingExplore();
        Permanent scout = findPermanent(player1, "River Herald Scout");

        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, scout.getId());
        harness.assertNotOnBattlefield(player1, "River Herald Scout");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exploreStillAllowsGraveyardChoiceAfterScoutLeavesBattlefield() {
        Card nonLand = new RiverHeraldScout();
        harness.setLibrary(player1, List.of(nonLand));
        castScoutWithoutResolvingExplore();
        Permanent scout = findPermanent(player1, "River Herald Scout");

        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, scout.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "River Herald Scout");
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonLand.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castRiverHeraldScout() {
        castScoutWithoutResolvingExplore();
        harness.passBothPriorities();
    }

    private void castScoutWithoutResolvingExplore() {
        harness.setHand(player1, List.of(new RiverHeraldScout()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
