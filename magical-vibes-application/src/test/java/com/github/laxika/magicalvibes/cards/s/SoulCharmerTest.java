package com.github.laxika.magicalvibes.cards.s;

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

class SoulCharmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when the damaged creature's controller declines to pay")
    void gainsLifeWhenDamagedCreatureControllerDeclinesToPay() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to prevent the life gain")
    void payingPreventsLifeGain() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        resolveCombatToPaymentChoice();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Soul Charmer")
    void combatDamageToPlayerDoesNotTrigger() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
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
