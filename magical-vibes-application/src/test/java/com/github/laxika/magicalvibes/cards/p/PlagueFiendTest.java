package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlagueFiendTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to survive")
    void payingPreventsDestruction() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanent(player2, "Giant Spider")).isNotNull();
    }

    @Test
    @DisplayName("The damaged creature is destroyed when its controller declines to pay")
    void decliningDestroysDamagedCreature() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Plague Fiend")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent plagueFiend = addCreatureReady(player1, new PlagueFiend());
        plagueFiend.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveUnblockedCombat();

        assertThat(findPermanent(player2, "Giant Spider")).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveCombatToPaymentChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveUnblockedCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
