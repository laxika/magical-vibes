package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnflukeTest extends BaseCardTest {

    private Permanent addBears(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB prevents the next 3 damage to the target and Dawnfluke stays on the battlefield")
    void hardcastAppliesShieldAndStays() {
        Permanent bears = addBears(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dawnfluke"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dawnfluke"));
    }

    @Test
    @DisplayName("ETB can protect the caster: prevention shield is applied to a targeted player")
    void hardcastCanTargetPlayer() {
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying only {W}, ETB still applies the prevention shield")
    void evokeAppliesShield() {
        Permanent bears = addBears(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (prevent + evoke sacrifice)

        assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Evoke: Dawnfluke is sacrificed as it enters")
    void evokeSacrificesSelf() {
        Permanent bears = addBears(player1);
        harness.setHand(player1, List.of(new Dawnfluke()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithEvoke(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dawnfluke"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dawnfluke"));
    }
}
