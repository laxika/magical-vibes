package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TectonicGiant.class, Forest.class, Shock.class, ElaborateFirecannon.class})
class TectonicGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can deal 3 damage to each opponent")
    void attackingDealsDamageToEachOpponent() {
        addReadyGiant();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature deals 3 damage to each opponent.");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 6);
    }

    @Test
    @DisplayName("Attacking exiles the top two cards and grants play permission to the chosen card")
    void attackingExilesTopTwoAndGrantsChosenCardPermission() {
        addReadyGiant();
        Forest chosen = new Forest();
        Forest other = new Forest();
        harness.setLibrary(player1, List.of(chosen, other));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Exile the top two cards of your library. Choose one of them. Until the end of your next turn, you may play that card.");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen, other);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(other.getId());
    }

    @Test
    @DisplayName("An opponent's spell targeting it triggers the ability")
    void opponentSpellTargetingItTriggersAbility() {
        Permanent giant = addReadyGiant();
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, giant.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "This creature deals 3 damage to each opponent.");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
    }

    @Test
    @DisplayName("An opponent's activated ability targeting it does not trigger the ability")
    void opponentAbilityTargetingItDoesNotTriggerAbility() {
        Permanent giant = addReadyGiant();
        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, giant.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyGiant() {
        return addCreatureReady(player1, new TectonicGiant());
    }
}
