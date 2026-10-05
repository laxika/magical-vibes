package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mindmelter.class, Wastes.class})
class MindmelterTest extends BaseCardTest {

    @Test
    @DisplayName("Mindmelter cannot be blocked")
    void cannotBeBlocked() {
        Permanent mindmelter = addReadyMindmelter();
        Permanent blocker = addCreatureReady(player2, new Mindmelter());
        mindmelter.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mindmelter);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Target opponent chooses a card to exile from their hand")
    void targetOpponentExilesCardOfTheirChoice() {
        addReadyMindmelter();
        Card creature = new Mindmelter();
        Card land = new Wastes();
        harness.setHand(player2, List.of(creature, land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Mindmelter's ability can only be activated as a sorcery")
    void abilityRequiresSorcerySpeed() {
        addReadyMindmelter();
        harness.setHand(player2, List.of(new Wastes()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Mindmelter's ability can target only an opponent")
    void abilityCannotTargetController() {
        addReadyMindmelter();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("An empty-handed opponent is a legal target and exiles nothing")
    void emptyHandDoesNotRequireAChoice() {
        addReadyMindmelter();
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana can pay the generic cost but cannot replace colorless mana")
    void coloredManaCannotPayColorlessCost() {
        addReadyMindmelter();
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Mindmelter can activate with three colored and one colorless mana")
    void activationDoesNotRequireTappingOrHaste() {
        harness.addToBattlefield(player1, new Mindmelter());
        Permanent mindmelter = gd.playerBattlefields.get(player1.getId()).getFirst();
        mindmelter.setSummoningSick(true);
        mindmelter.setTapped(true);
        Card land = new Wastes();
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(mindmelter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mindmelter cannot activate during an opponent's main phase")
    void abilityRequiresControllersTurn() {
        addReadyMindmelter();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mindmelter cannot activate with another ability on the stack")
    void abilityRequiresEmptyStack() {
        addReadyMindmelter();
        harness.setHand(player2, List.of(new Wastes()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
    }

    @Test
    @DisplayName("An activated ability still exiles a card after Mindmelter leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent mindmelter = addReadyMindmelter();
        Card land = new Wastes();
        harness.setHand(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mindmelter);
        gd.playerGraveyards.get(player1.getId()).add(mindmelter.getCard());

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
    }

    private Permanent addReadyMindmelter() {
        return addCreatureReady(player1, new Mindmelter());
    }
}