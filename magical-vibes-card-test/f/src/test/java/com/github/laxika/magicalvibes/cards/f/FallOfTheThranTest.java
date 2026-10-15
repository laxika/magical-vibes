package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({FallOfTheThran.class, Plains.class, Island.class, Mountain.class, Forest.class, BalothGorger.class})
class FallOfTheThranTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys all lands on the battlefield for both players")
    void chapterIDestroysAllLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new FallOfTheThran()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → chapter I triggers

        GameData gd = harness.getGameData();
        // Chapter I should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter I"));

        harness.passBothPriorities(); // resolve chapter I

        gd = harness.getGameData();

        // All lands should be destroyed
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isZero();
        assertThat(p2Lands).isZero();

        // Lands should be in graveyards
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        long p2GraveyardLands = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isEqualTo(2);
        assertThat(p2GraveyardLands).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter II returns all available lands when each graveyard has at most two")
    void chapterIIReturnsLandsAutoWhenTwoOrFewer() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 1);

        // Put 2 lands in each player's graveyard
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), new Island())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Mountain(), new Forest())));

        // Advance to precombat main to trigger chapter II
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to precombat main → chapter II triggers

        GameData gd = harness.getGameData();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("chapter II"));

        // Resolve chapter II
        harness.passBothPriorities();

        gd = harness.getGameData();

        // Both players should have their lands back on the battlefield
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(2);
        assertThat(p2Lands).isEqualTo(2);

        // Graveyards should have no lands
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        long p2GraveyardLands = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isZero();
        assertThat(p2GraveyardLands).isZero();
    }

    @Test
    @DisplayName("Chapter II returns only one land when player has exactly one in graveyard")
    void chapterIIReturnsOneLandWhenOnlyOneAvailable() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 1);

        // Put 1 land in player1's graveyard, none in player2's
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains())));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II does nothing when no lands in any graveyard")
    void chapterIIDoesNothingWhenNoLandsInGraveyard() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 1);

        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isZero();
        assertThat(p2Lands).isZero();
    }

    @Test
    @DisplayName("Chapter II prompts choice when player has more than two lands in graveyard")
    void chapterIIPromptsChoiceWhenMoreThanTwoLands() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 1);

        // Put 3 lands in player1's graveyard (more than maxCount of 2)
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), new Island(), new Forest())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Mountain())));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II — player1 must choose (3 > 2), player2 auto-returns 1

        GameData gd = harness.getGameData();

        // Player1 should be prompted to choose (graveyard choice awaiting input)
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Player2 waits until player1 has completed their choices.
        long p2Lands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p2Lands).isZero();

        // Player1 chooses first land (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();
        // Player1 should be prompted again for second land
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Player1 chooses second land (now index 0 since previous was removed)
        harness.handleGraveyardCardChosen(player1, 0);

        gd = harness.getGameData();

        harness.assertOnBattlefield(player2, "Mountain");

        // Player1 should have 2 lands on the battlefield
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(2);

        // Player1 should still have 1 land in graveyard
        long p1GraveyardLands = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.hasType(CardType.LAND))
                .count();
        assertThat(p1GraveyardLands).isEqualTo(1);
    }

    @Test
    @DisplayName("Only land cards are returned, not other card types")
    void onlyLandCardsAreReturned() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 1);

        // Put a mix of lands and non-lands in graveyard
        Card creature = new BalothGorger();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains(), creature)));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter II triggers
        harness.passBothPriorities(); // resolve chapter II

        GameData gd = harness.getGameData();

        // Only the land should have been returned
        long p1Lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
        assertThat(p1Lands).isEqualTo(1);

        // Creature should still be in graveyard
        harness.assertInGraveyard(player1, "Baloth Gorger");
    }

    @Test
    @DisplayName("Saga is sacrificed after chapter III resolves")
    void sagaSacrificedAfterChapterIII() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        Permanent saga = findPermanent(player1, "Fall of the Thran");
        saga.setCounterCount(CounterType.LORE, 2);

        harness.setGraveyard(player1, new ArrayList<>(List.of(new Plains())));
        harness.setGraveyard(player2, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // precombat main → chapter III triggers

        // Chapter III on stack — saga should still be on battlefield
        harness.assertOnBattlefield(player1, "Fall of the Thran");

        harness.passBothPriorities(); // resolve chapter III

        harness.assertNotOnBattlefield(player1, "Fall of the Thran");
        harness.assertInGraveyard(player1, "Fall of the Thran");
    }

    @Test
    @DisplayName("Chapter III returns two lands for each player before the Saga is sacrificed")
    void chapterIIIReturnsLandsForBothPlayers() {
        harness.addToBattlefield(player1, new FallOfTheThran());
        findPermanent(player1, "Fall of the Thran").setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new Plains(), new Island(), new BalothGorger()));
        harness.setGraveyard(player2, List.of(new Mountain(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fall of the Thran");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertNotOnBattlefield(player1, "Fall of the Thran");
        harness.assertInGraveyard(player1, "Fall of the Thran");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Chapters II and III require both land returns when three lands are available")
    void landReturnsCannotBeDeclined(int initialLore) {
        harness.addToBattlefield(player1, new FallOfTheThran());
        findPermanent(player1, "Fall of the Thran").setCounterCount(CounterType.LORE, initialLore);
        harness.setGraveyard(player1, List.of(new Plains(), new Island(), new Forest()));
        harness.setGraveyard(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Forest");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("The active player chooses first and all selected lands enter after both players choose")
    void activePlayerChoosesFirstAndLandsReturnTogether(int initialLore) {
        harness.addToBattlefield(player2, new FallOfTheThran());
        findPermanent(player2, "Fall of the Thran").setCounterCount(CounterType.LORE, initialLore);
        harness.setGraveyard(player1, List.of(new Plains(), new Island(), new Forest()));
        harness.setGraveyard(player2, List.of(new Mountain(), new Island(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        if (initialLore == 2) {
            harness.assertNotOnBattlefield(player2, "Fall of the Thran");
            harness.assertInGraveyard(player2, "Fall of the Thran");
        }
    }

    @Test
    @DisplayName("Chapter I leaves creatures and enchantments on the battlefield")
    void chapterIDoesNotDestroyNonlands() {
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new FallOfTheThran()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Baloth Gorger");
        harness.assertOnBattlefield(player2, "Baloth Gorger");
        harness.assertOnBattlefield(player1, "Fall of the Thran");
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Plains");
    }
}
