package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Withstand.class, WildCantor.class})
class WithstandTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 3 damage to a target creature and draws a card")
    void preventsDamageToCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new WildCantor());
        harness.setHand(player1, List.of(new Withstand()));
        harness.setLibrary(player1, List.of(new WildCantor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Wild Cantor"));
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Wild Cantor");
        assertThat(target.getDamagePreventionShield()).isEqualTo(3);
        harness.assertInHand(player1, "Wild Cantor");
    }

    @Test
    @DisplayName("Prevents the next 3 damage to a target player and draws a card")
    void preventsDamageToPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new Withstand()));
        harness.setLibrary(player1, List.of(new WildCantor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        harness.assertInHand(player1, "Wild Cantor");
    }

    @Test
    @DisplayName("Prevents the next 3 damage to a target player")
    void preventsNextDamageToPlayer() {
        Permanent attacker = addCreatureReady(player1, new WildCantor());
        harness.setHand(player1, List.of(new Withstand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 2);
    }
}
