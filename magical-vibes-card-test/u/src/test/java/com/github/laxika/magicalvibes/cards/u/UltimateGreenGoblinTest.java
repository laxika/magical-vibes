package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimateGreenGoblin.class, GrizzlyBears.class})
class UltimateGreenGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's upkeep, its controller discards a card and creates a Treasure")
    void discardsAndCreatesTreasureAtUpkeep() {
        harness.addToBattlefield(player1, new UltimateGreenGoblin());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Mayhem casts it from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        UltimateGreenGoblin goblin = new UltimateGreenGoblin();
        harness.setGraveyard(player1, List.of(goblin));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(goblin.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ultimate Green Goblin")).hasSize(1);
    }

    @Test
    @DisplayName("Mayhem cannot cast it from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new UltimateGreenGoblin()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsTreasureEvenWithAnEmptyHand() {
        harness.addToBattlefield(player1, new UltimateGreenGoblin());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new UltimateGreenGoblin());
        harness.setHand(player1, List.of(new UltimateGreenGoblin()));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void mayhemCanBePaidWithRedMana() {
        UltimateGreenGoblin goblin = new UltimateGreenGoblin();
        harness.setGraveyard(player1, List.of(goblin));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(goblin.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ultimate Green Goblin")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void mayhemDoesNotAllowCastingDuringUpkeep() {
        UltimateGreenGoblin goblin = new UltimateGreenGoblin();
        harness.setGraveyard(player1, List.of(goblin));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(goblin.getId())));
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ultimate Green Goblin");
    }

    @Test
    void mayhemRequiresAColoredManaForTheHybridSymbol() {
        UltimateGreenGoblin goblin = new UltimateGreenGoblin();
        harness.setGraveyard(player1, List.of(goblin));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(goblin.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ultimate Green Goblin");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
