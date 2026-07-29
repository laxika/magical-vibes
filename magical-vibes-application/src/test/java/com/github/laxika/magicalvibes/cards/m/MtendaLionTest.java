package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MtendaLionTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player pays {U} — no combat damage is dealt by the Lion")
    void defendingPlayerPaysToPreventDamage() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Defending player declines — combat damage is dealt normally")
    void declinedPaymentLetsDamageThrough() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, false);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Without the mana to pay, the acceptance fails and damage is dealt")
    void acceptingWithoutManaDealsDamage() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention also stops damage dealt to a blocking creature")
    void paidPreventionStopsDamageToBlocker() {
        addCreatureReady(player1, new MtendaLion());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
