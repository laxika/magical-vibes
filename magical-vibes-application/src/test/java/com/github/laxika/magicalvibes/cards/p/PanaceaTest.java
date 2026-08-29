package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PanaceaTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents X damage to a target creature")
    void preventsXDamageToTargetCreature() {
        harness.addToBattlefield(player1, new Panacea());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        assertThat(findPermanent(player1, "Panacea").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents X damage to a target player")
    void preventsXDamageToTargetPlayer() {
        harness.addToBattlefield(player1, new Panacea());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("The prevention ability cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new Panacea());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The prevention shield prevents damage dealt later this turn")
    void preventsLaterDamage() {
        harness.addToBattlefield(player1, new Panacea());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }
}
