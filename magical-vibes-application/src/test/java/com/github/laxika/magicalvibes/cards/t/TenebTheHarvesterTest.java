package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenebTheHarvester.class, GrizzlyBears.class, HolyDay.class})
class TenebTheHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{B} returns a target creature card from any graveyard to the battlefield")
    void payingReturnsTargetCreatureFromOpponentGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        dealCombatDamageWithTeneb();
        addManaForTeneb();
        chooseTarget(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Declining the payment leaves the targeted creature card in its graveyard")
    void decliningPaymentLeavesTargetInGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        addManaForTeneb();

        dealCombatDamageWithTeneb();
        chooseTarget(target);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The trigger cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Card target = new HolyDay();
        harness.setGraveyard(player2, List.of(target));

        dealCombatDamageWithTeneb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The trigger does not return a creature card that leaves the graveyard")
    void fizzlesWhenTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        addManaForTeneb();

        dealCombatDamageWithTeneb();
        chooseTarget(target);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForTeneb() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void dealCombatDamageWithTeneb() {
        Permanent teneb = addCreatureReady(player1, new TenebTheHarvester());
        teneb.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    private void chooseTarget(Card target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }
}
