package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorbranThaneOfRedFell.class, GrizzlyBears.class, Shock.class})
class TorbranThaneOfRedFellTest extends BaseCardTest {

    @Test
    @DisplayName("Red combat damage to an opponent gets increased by 2")
    void redCombatDamageToOpponentGetsIncreased() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TorbranThaneOfRedFell());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Red spell damage to an opponent gets increased by 2")
    void redSpellDamageToOpponentGetsIncreased() {
        harness.addToBattlefield(player1, new TorbranThaneOfRedFell());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Red damage to an opponent's permanent gets increased by 2")
    void redDamageToOpponentPermanentGetsIncreased() {
        harness.addToBattlefield(player1, new TorbranThaneOfRedFell());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nonred source damage is not increased")
    void nonredSourceDamageIsNotIncreased() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TorbranThaneOfRedFell());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to the controller is not increased")
    void damageToControllerIsNotIncreased() {
        harness.addToBattlefield(player1, new TorbranThaneOfRedFell());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
