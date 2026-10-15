package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireElemental.class, GreenwoodSentinel.class, Shock.class})
class WildfireElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent noncombat damage gives creatures you control +1/+0")
    void opponentNoncombatDamageBoostsOwnCreatures() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new WildfireElemental());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void damageToControllerDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(elemental.getPowerModifier()).isZero();
    }

    @Test
    void damageToOpponentCreatureDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
        assertThat(elemental.getPowerModifier()).isZero();
    }

    @Test
    void opponentsOwnDamageSourceStillTriggers() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(elemental.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    void eachDamageEventBoostsOnceRegardlessOfDamageAmount() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    void boostAffectsCreaturesPresentAtResolutionOnly() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(elemental.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
    }

    @Test
    void combatDamageDoesNotTrigger() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        elemental.setSummoningSick(false);
        elemental.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(elemental.getPowerModifier()).isZero();
    }

    @Test
    void eachElementalTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WildfireElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
    }
}
