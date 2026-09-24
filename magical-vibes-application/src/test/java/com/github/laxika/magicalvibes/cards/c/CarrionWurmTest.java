package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;

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

@CardUsed(CarrionWurm.class)
class CarrionWurmTest extends BaseCardTest {

    @Test
    @DisplayName("A player may exile three cards when Carrion Wurm attacks")
    void playerMayExileThreeCardsWhenWurmAttacks() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        CarrionWurm first = new CarrionWurm();
        CarrionWurm second = new CarrionWurm();
        CarrionWurm third = new CarrionWurm();
        harness.setGraveyard(player2, List.of(first, second, third));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(wurm.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A player with fewer than three graveyard cards is not offered Carrion Wurm's choice")
    void fewerThanThreeCardsDoesNotOfferChoice() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        CarrionWurm first = new CarrionWurm();
        CarrionWurm second = new CarrionWurm();
        harness.setGraveyard(player2, List.of(first, second));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(wurm.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Declining Carrion Wurm's choice leaves it able to deal combat damage")
    void decliningChoiceDoesNotSuppressCombatDamage() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        harness.setGraveyard(player1, List.of(new CarrionWurm(), new CarrionWurm(), new CarrionWurm()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(wurm.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Exactly three cards are exiled when a player has more than three available")
    void acceptingChoiceExilesExactlyThreeCards() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        CarrionWurm first = new CarrionWurm();
        CarrionWurm second = new CarrionWurm();
        CarrionWurm third = new CarrionWurm();
        CarrionWurm fourth = new CarrionWurm();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(wurm.getId());
    }

    @Test
    @DisplayName("Each player receives Carrion Wurm's choice in turn order")
    void eachPlayerReceivesChoiceInTurnOrder() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        List<Card> ownCards = List.of(new CarrionWurm(), new CarrionWurm(), new CarrionWurm());
        List<Card> opponentCards = List.of(new CarrionWurm(), new CarrionWurm(), new CarrionWurm());
        harness.setGraveyard(player1, ownCards);
        harness.setGraveyard(player2, opponentCards);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(ownCards);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(opponentCards);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(wurm.getId());
    }

    @Test
    @DisplayName("Carrion Wurm's blocking trigger also offers the three-card choice")
    void blockingTriggersGraveyardExileChoice() {
        Permanent attacker = addCreatureReady(player1, new CarrionWurm());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new CarrionWurm());
        CarrionWurm first = new CarrionWurm();
        CarrionWurm second = new CarrionWurm();
        CarrionWurm third = new CarrionWurm();
        harness.setGraveyard(player2, List.of(first, second, third));

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(wurm.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wurm);
    }
}
