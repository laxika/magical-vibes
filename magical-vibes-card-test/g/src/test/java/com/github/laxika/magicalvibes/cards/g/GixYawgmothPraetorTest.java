package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixYawgmothPraetor.class, GixianInfiltrator.class})
class GixYawgmothPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature you control may be paid for with 1 life to draw")
    void combatDamageMayBePaidForWithLifeToDraw() {
        Permanent attacker = addCreatureReady(player1, new GixianInfiltrator());
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Discarding X cards exiles X cards and offers play during resolution")
    void discardXExilesXAndOffersPlayDuringResolution() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator(), new GixianInfiltrator()));
        Card exiled = new GixianInfiltrator();
        Card secondExiled = new GixianInfiltrator();
        harness.setLibrary(player2, List.of(exiled, secondExiled));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(exiled, secondExiled);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();

    }

    @Test
    @DisplayName("Choosing zero for X requires no discard and exiles no cards")
    void zeroXRequiresNoDiscardAndExilesNoCards() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of());
        Card libraryCard = new GixianInfiltrator();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingAbilityActivation).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Declining the combat damage payment neither pays life nor draws")
    void decliningPaymentDoesNotPayOrDraw() {
        Permanent attacker = addCreatureReady(player1, new GixianInfiltrator());
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Gix's own combat damage triggers the life payment and draw")
    void ownCombatDamageTriggersDraw() {
        Permanent attacker = addCreatureReady(player1, new GixYawgmothPraetor());
        attacker.setAttacking(true);
        Card drawn = new GixianInfiltrator();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Activation on an opponent's turn offers a creature spell during resolution")
    void offersCreatureSpellOnOpponentsTurn() {
        harness.addToBattlefield(player1, new GixYawgmothPraetor());
        harness.setHand(player1, List.of(new GixianInfiltrator()));
        Card exiled = new GixianInfiltrator();
        harness.setLibrary(player2, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, 1, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
