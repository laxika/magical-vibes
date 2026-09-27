package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlourishingStrike.class, AirElemental.class, GrizzlyBears.class, Millstone.class})
class FlourishingStrikeTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(new int[]{0}, List.of(target.getId()), false);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()), false);

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void entwineResolvesBothModesAndPaysAdditionalCost() {
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(flyingCreature.getId(), creature.getId()), true);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void damageModeCannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FlourishingStrike()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostModeCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new FlourishingStrike()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new FlourishingStrike()));
        harness.addMana(player1, ManaColor.GREEN, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 3 : 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
