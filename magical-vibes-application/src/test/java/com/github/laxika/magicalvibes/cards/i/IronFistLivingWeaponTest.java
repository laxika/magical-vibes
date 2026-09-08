package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronFistLivingWeapon.class, GiantGrowth.class, GrizzlyBears.class})
class IronFistLivingWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell targeting a creature you control grants the tap damage ability")
    void grantsTapDamageAbilityWhenSpellTargetsControlledCreature() {
        Permanent ironFist = addCreatureReady(player1, new IronFistLivingWeapon());
        Permanent controlledCreature = addCreatureReady(player1, new GrizzlyBears());

        castGiantGrowthAt(controlledCreature, player1);
        resolveCastTriggerAndSpell();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ironFist),
                null, player2.getId());
        harness.passBothPriorities();

        assertThat(ironFist.isTapped()).isTrue();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The damage uses Iron Fist's current power")
    void damageUsesCurrentPower() {
        Permanent ironFist = addCreatureReady(player1, new IronFistLivingWeapon());

        castGiantGrowthAt(ironFist, player1);
        resolveCastTriggerAndSpell();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ironFist),
                null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A spell targeting an opponent's creature does not trigger Iron Fist")
    void doesNotTriggerForOpponentCreature() {
        Permanent ironFist = addCreatureReady(player1, new IronFistLivingWeapon());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castGiantGrowthAt(opponentCreature, player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ironFist), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent ironFist = addCreatureReady(player1, new IronFistLivingWeapon());

        castGiantGrowthAt(ironFist, player1);
        resolveCastTriggerAndSpell();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ironFist), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private void castGiantGrowthAt(Permanent target, com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new GiantGrowth()));
        harness.addMana(caster, ManaColor.GREEN, 1);
        harness.castInstant(caster, 0, target.getId());
    }

    private void resolveCastTriggerAndSpell() {
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
