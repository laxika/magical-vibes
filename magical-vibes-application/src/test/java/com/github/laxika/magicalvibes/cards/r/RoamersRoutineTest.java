package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearerOfGlory;
import com.github.laxika.magicalvibes.cards.b.BlossomingSands;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoamersRoutine.class, Forest.class, BearerOfGlory.class, BlossomingSands.class, Island.class, Plains.class})
class RoamersRoutineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers basic lands to enter the battlefield tapped")
    void resolvesBasicLandSearch() {
        harness.setHand(player1, List.of(new RoamersRoutine()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Plains plains = new Plains();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island, new BearerOfGlory(), new BlossomingSands()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, forest, island);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        harness.assertInGraveyard(player1, "Roamer's Routine");
    }

    @Test
    @DisplayName("Harmonize taps a creature to reduce the cost and exiles the spell")
    void harmonizeSearchesAndExilesSpell() {
        BearerOfGlory creature = new BearerOfGlory();
        harness.addToBattlefield(player1, creature);
        harness.setGraveyard(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new BearerOfGlory()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithTapCost(player1, 0,
                List.of(harness.getPermanentId(player1, "Bearer of Glory")));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature && permanent.isTapped());
        harness.assertNotInGraveyard(player1, "Roamer's Routine");
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roamer's Routine"));
    }

    @Test
    void harmonizeWithoutTappingPaysFullCost() {
        harness.setGraveyard(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        harness.assertNotInGraveyard(player1, "Roamer's Routine");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roamer's Routine"));
    }

    @Test
    void mayFailToFindEvenWithBasicLandAvailable() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Roamer's Routine");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWhenLibraryHasNoBasicLands() {
        BlossomingSands land = new BlossomingSands();
        harness.setHand(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Blossoming Sands");
        harness.assertInGraveyard(player1, "Roamer's Routine");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void harmonizeCanTapSummoningSickCreatureAndPaysAtCasting() {
        var creature = harness.addToBattlefieldAndReturn(player1, new BearerOfGlory());
        creature.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new RoamersRoutine()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roamer's Routine"));
    }

    @Test
    void harmonizeRejectsTappedCreature() {
        var creature = harness.addToBattlefieldAndReturn(player1, new BearerOfGlory());
        creature.tap();
        harness.setGraveyard(player1, List.of(new RoamersRoutine()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Roamer's Routine");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
