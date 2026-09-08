package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CarrionRats.class, GrizzlyBears.class, HillGiant.class})
class CarrionRatsTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent may exile a card when Carrion Rats attacks")
    void opponentMayExileCardWhenRatsAttack() {
        Permanent rats = addCreatureReady(player1, new CarrionRats());
        Card card = new HillGiant();
        harness.setGraveyard(player2, List.of(card));

        attackUnblocked(rats);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(rats.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An accepted choice can select one of several graveyard cards")
    void acceptedChoiceSelectsOneOfSeveralGraveyardCards() {
        Permanent rats = addCreatureReady(player1, new CarrionRats());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setGraveyard(player2, List.of(first, second));

        attackUnblocked(rats);

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(rats.getId());
    }

    @Test
    @DisplayName("Each player receives the choice in turn order")
    void eachPlayerReceivesChoiceInTurnOrder() {
        Permanent rats = addCreatureReady(player1, new CarrionRats());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new HillGiant();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        attackUnblocked(rats);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(rats.getId());
    }

    @Test
    @DisplayName("Declining the graveyard exile leaves Carrion Rats able to deal combat damage")
    void decliningExileDoesNotSuppressCombatDamage() {
        Permanent rats = addCreatureReady(player1, new CarrionRats());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card));

        attackUnblocked(rats);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(rats.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The block trigger also lets a player exile a card")
    void blockingTriggersGraveyardExileChoice() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);
        Permanent rats = addCreatureReady(player2, new CarrionRats());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(rats.getId());
    }

    private void attackUnblocked(Permanent rats) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rats)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
