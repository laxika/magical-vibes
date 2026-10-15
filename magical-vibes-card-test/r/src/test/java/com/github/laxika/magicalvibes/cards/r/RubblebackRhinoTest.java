package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.IzzetStaticaster;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RubblebackRhino.class, GiantGrowth.class, IzzetStaticaster.class, SupremeVerdict.class})
class RubblebackRhinoTest extends BaseCardTest {

    @Test
    void opponentCannotTargetRhinoWithSpell() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RubblebackRhino());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, rhino.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rubbleback Rhino");
    }

    @Test
    void controllerCanTargetRhinoWithSpell() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RubblebackRhino());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, rhino.getId());

        assertThat(rhino.getPowerModifier()).isEqualTo(3);
        assertThat(rhino.getToughnessModifier()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void opponentCannotTargetRhinoWithAbility() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RubblebackRhino());
        addCreatureReady(player2, new IzzetStaticaster());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, rhino.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(rhino.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetRhinoWithAbility() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RubblebackRhino());

        harness.activateAbility(player1, 0, null, rhino.getId());
        harness.passBothPriorities();

        assertThat(rhino.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rubbleback Rhino");
    }

    @Test
    void hexproofDoesNotPreventUntargetedDamageToSameNameCreature() {
        addCreatureReady(player2, new IzzetStaticaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RubblebackRhino());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(rhino.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rubbleback Rhino");
    }

    @Test
    void hexproofDoesNotPreventOpponentsUntargetedRemoval() {
        harness.addToBattlefield(player1, new RubblebackRhino());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SupremeVerdict()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "Rubbleback Rhino");
        harness.assertInGraveyard(player1, "Rubbleback Rhino");
    }
}
