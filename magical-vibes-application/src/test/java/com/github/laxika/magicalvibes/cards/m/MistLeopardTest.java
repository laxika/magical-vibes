package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistLeopard.class, GiantGrowth.class, LightningBolt.class,
        ProdigalPyromancer.class, Pyroclasm.class})
class MistLeopardTest extends BaseCardTest {

    @Test
    void controllerCannotTargetWithGiantGrowth() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new MistLeopard());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, leopard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void opponentCannotTargetWithLightningBolt() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new MistLeopard());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, leopard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void controllerCannotTargetWithActivatedAbility() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new MistLeopard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, leopard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void opponentCannotTargetWithActivatedAbility() {
        Permanent leopard = harness.addToBattlefieldAndReturn(player1, new MistLeopard());
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, leopard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudDoesNotPreventUntargetedDamage() {
        harness.addToBattlefield(player1, new MistLeopard());
        harness.addToBattlefield(player2, new MistLeopard());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mist Leopard");
        harness.assertNotOnBattlefield(player2, "Mist Leopard");
        harness.assertInGraveyard(player1, "Mist Leopard");
        harness.assertInGraveyard(player2, "Mist Leopard");
    }
}
