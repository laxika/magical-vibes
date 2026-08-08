package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlamesOfTheBloodHandTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to the targeted player and stops only that player gaining life")
    void deals4AndLocksTargetPlayerLifeGain() {
        harness.setHand(player1, List.of(new FlamesOfTheBloodHand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
        // The lock is per-player: the caster is unaffected.
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("A life gain spell the targeted player resolves afterwards gains nothing")
    void laterLifeGainByTargetIsIgnored() {
        harness.setHand(player1, List.of(new FlamesOfTheBloodHand()));
        harness.setHand(player2, List.of(new RenewedFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Renewed Faith");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The damage can't be prevented")
    void damageIsUnpreventable() {
        gd.playersWithAllDamagePrevented.add(player2.getId());

        harness.setHand(player1, List.of(new FlamesOfTheBloodHand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The life gain lock wears off at end of turn")
    void lockClearedAtEndOfTurn() {
        gd.playersWhoCantGainLifeThisTurn.add(player2.getId());

        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersWhoCantGainLifeThisTurn).isEmpty();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }
}
