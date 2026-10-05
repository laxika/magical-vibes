package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KalonianBehemoth.class, GiantGrowth.class, ProdigalPyromancer.class, Pyroclasm.class})
class KalonianBehemothTest extends BaseCardTest {

    @Test
    void controllerCannotTargetWithSpell() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new KalonianBehemoth());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, behemoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void opponentCannotTargetWithSpell() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, behemoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void controllerCannotTargetWithActivatedAbility() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new KalonianBehemoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, behemoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void opponentCannotTargetWithActivatedAbility() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent behemoth = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, behemoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudDoesNotPreventUntargetedDamage() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth());
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(behemoth.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Kalonian Behemoth");
    }
}
