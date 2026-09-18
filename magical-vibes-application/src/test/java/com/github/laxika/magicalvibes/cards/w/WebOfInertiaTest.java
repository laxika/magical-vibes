package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebOfInertia.class, SuntailHawk.class})
class WebOfInertiaTest extends BaseCardTest {

    @Test
    @DisplayName("The active opponent may exile a graveyard card to allow attacks")
    void activeOpponentMayExileCard() {
        Card graveyardCard = new SuntailHawk();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addToBattlefield(player1, new WebOfInertia());
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("The active opponent chooses which card to exile when several are available")
    void activeOpponentChoosesGraveyardCard() {
        Card first = new SuntailHawk();
        Card second = new SuntailHawk();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addToBattlefield(player1, new WebOfInertia());

        resolveCombatTrigger(player2);
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Declining the exile prevents the active opponent's creatures from attacking")
    void decliningPreventsAttacks() {
        harness.addToBattlefield(player1, new WebOfInertia());
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);
        harness.handleMayAbilityChosen(player2, false);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An accepted choice with an empty graveyard still prevents attacks")
    void emptyGraveyardAppliesRestriction() {
        harness.setGraveyard(player2, List.of());
        harness.addToBattlefield(player1, new WebOfInertia());
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);
        harness.handleMayAbilityChosen(player2, true);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new WebOfInertia());
        addCreatureReady(player2, new SuntailHawk());

        resolveCombatTrigger(player2);
        harness.handleMayAbilityChosen(player2, false);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireEndOfTurnFloatingEffects();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("The ability does not trigger on the Web controller's turn")
    void doesNotTriggerOnControllerTurn() {
        harness.addToBattlefield(player1, new WebOfInertia());
        addCreatureReady(player1, new SuntailHawk());

        resolveCombatTrigger(player1);

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        declareAttackers(player1, List.of(1));
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("The restriction does not prevent attacks at the controller's planeswalker")
    void restrictionDoesNotPreventAttackingControllerPlaneswalker() {
        harness.addToBattlefield(player1, new WebOfInertia());
        Permanent attacker = addCreatureReady(player2, new SuntailHawk());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        resolveCombatTrigger(player2);
        harness.handleMayAbilityChosen(player2, false);

        declareAttackerAtTarget(player2, attacker, planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private void resolveCombatTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void declareAttackerAtTarget(Player attacker, Permanent creature, Permanent target) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attacker.getId()).indexOf(creature);
        gs.declareAttackers(gd, attacker, List.of(attackerIndex), Map.of(attackerIndex, target.getId()));
    }
}
