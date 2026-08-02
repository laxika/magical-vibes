package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkullcrackTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player and locks life gain / damage prevention for the turn")
    void deals3AndLocksLifeGainAndPrevention() {
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playersCantGainLifeThisTurn).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    @DisplayName("A life gain spell resolved afterwards gains nothing")
    void laterLifeGainIsIgnored() {
        harness.setHand(player1, List.of(new Skullcrack(), new RenewedFaith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Renewed Faith");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The life gain lock wears off at end of turn")
    void lockClearedAtEndOfTurn() {
        gd.playersCantGainLifeThisTurn = true;

        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersCantGainLifeThisTurn).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }
}
