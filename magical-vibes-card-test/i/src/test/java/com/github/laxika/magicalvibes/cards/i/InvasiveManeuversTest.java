package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.g.GlacierGodmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvasiveManeuvers.class, EntropicBattlecruiser.class, GlacierGodmaw.class})
class InvasiveManeuversTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageWithSpacecraft() {
        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void opponentsSpacecraftDoesNotIncreaseDamage() {
        harness.addToBattlefield(player2, new EntropicBattlecruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void spacecraftEnteringBeforeResolutionIncreasesDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void spacecraftLeavingBeforeResolutionReducesDamage() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(spacecraft);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canDamageOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlacierGodmaw());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetUnstationedSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EntropicBattlecruiser());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDealDamageWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlacierGodmaw());
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Invasive Maneuvers");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new InvasiveManeuvers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
