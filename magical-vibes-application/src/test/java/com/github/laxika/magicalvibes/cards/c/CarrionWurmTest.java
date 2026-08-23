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

@CardUsed({CarrionWurm.class, GrizzlyBears.class, HillGiant.class})
class CarrionWurmTest extends BaseCardTest {

    @Test
    @DisplayName("A player may exile three cards when Carrion Wurm attacks")
    void playerMayExileThreeCardsWhenWurmAttacks() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
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
    }

    @Test
    @DisplayName("A player with fewer than three graveyard cards is not offered Carrion Wurm's choice")
    void fewerThanThreeCardsDoesNotOfferChoice() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setGraveyard(player2, List.of(first, second));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(wurm.getId());
    }

    @Test
    @DisplayName("Declining Carrion Wurm's choice leaves it able to deal combat damage")
    void decliningChoiceDoesNotSuppressCombatDamage() {
        Permanent wurm = addCreatureReady(player1, new CarrionWurm());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(wurm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Carrion Wurm's blocking trigger also offers the three-card choice")
    void blockingTriggersGraveyardExileChoice() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);
        Permanent wurm = addCreatureReady(player2, new CarrionWurm());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(wurm.getId());
    }
}
