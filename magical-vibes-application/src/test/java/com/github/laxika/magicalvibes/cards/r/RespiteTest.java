package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage and gains 1 life per attacking creature")
    void preventsCombatDamageAndGainsLifePerAttacker() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Respite()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0, 1));
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when no creature is attacking")
    void gainsNoLifeWithoutAttackers() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Respite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Respite");
    }

    @Test
    @DisplayName("Counts attacking creatures regardless of who controls them")
    void countsAttackersControlledByTheCaster() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Respite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
