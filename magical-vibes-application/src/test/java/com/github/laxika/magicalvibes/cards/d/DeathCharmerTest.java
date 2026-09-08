package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathCharmerTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged creature's controller loses 2 life when they decline to pay")
    void losesLifeWhenDamagedCreatureControllerDeclinesToPay() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to prevent losing life")
    void payingPreventsLifeLoss() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Death Charmer")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveCombatToPaymentChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
