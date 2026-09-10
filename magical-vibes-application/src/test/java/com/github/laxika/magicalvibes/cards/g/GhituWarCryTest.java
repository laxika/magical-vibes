package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LoneWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhituWarCry.class, LoneWolf.class})
class GhituWarCryTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusOnePowerUntilEndOfTurn() {
        Permanent warCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new LoneWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warCry), 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    void doesNotRequireTappingTheSource() {
        Permanent warCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        warCry.tap();
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new LoneWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warCry), 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(warCry.isTapped()).isTrue();
    }

    @Test
    void requiresRedManaToActivate() {
        Permanent warCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new LoneWolf());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(warCry),
                0,
                null,
                wolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent warCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new LoneWolf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(warCry), 0, null, wolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
    }

    @Test
    void cannotTargetAnEnchantment() {
        Permanent warCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        Permanent otherWarCry = harness.addToBattlefieldAndReturn(player1, new GhituWarCry());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(warCry),
                0,
                null,
                otherWarCry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
