package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlePlan.class, Forest.class, GrizzlyBears.class, Mountain.class})
class BattlePlanTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("The beginning-of-combat trigger gives a creature you control +2/+0")
    void beginningOfCombatBoostsOwnCreature() {
        harness.addToBattlefield(player1, new BattlePlan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The trigger rejects a creature controlled by an opponent")
    void beginningOfCombatRejectsOpponentCreature() {
        harness.addToBattlefield(player1, new BattlePlan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BattlePlan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic landcycling discards Battle Plan and offers basic lands")
    void basicLandcyclingSearchesForBasicLand() {
        BattlePlan battlePlan = new BattlePlan();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(battlePlan));
        harness.setLibrary(player1, List.of(forest, mountain, bears));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, mountain);
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Battle Plan");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Battle Plan does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new BattlePlan());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Basic landcycling discards as a cost before the search resolves")
    void basicLandcyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new BattlePlan()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Battle Plan");
        harness.assertInGraveyard(player1, "Battle Plan");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling may fail to find even when a basic land is available")
    void basicLandcyclingMayFailToFind() {
        Card forest = new Forest();
        harness.setHand(player1, List.of(new BattlePlan()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Battle Plan");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Basic landcycling resolves with an empty library")
    void basicLandcyclingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new BattlePlan()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Battle Plan");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Basic landcycling requires red mana and does not discard on failed payment")
    void basicLandcyclingRequiresRedMana() {
        harness.setHand(player1, List.of(new BattlePlan()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Battle Plan");
        harness.assertNotInGraveyard(player1, "Battle Plan");
        assertThat(gd.stack).isEmpty();
    }
}
