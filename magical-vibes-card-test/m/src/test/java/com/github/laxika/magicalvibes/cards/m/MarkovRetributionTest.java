package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.v.VoldarenBloodcaster;
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

@CardUsed({MarkovRetribution.class, VoldarenBloodcaster.class, DawnhartDisciple.class})
class MarkovRetributionTest extends BaseCardTest {

    @Test
    @DisplayName("The pump mode boosts all creatures you control")
    void pumpModeBoostsOwnCreatures() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent ownCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartDisciple());

        cast(new int[]{0}, List.of());

        assertThat(vampire.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The damage mode has a Vampire deal damage equal to its power")
    void damageModeDealsVampiresPower() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());

        cast(new int[]{1}, List.of(vampire.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(vampire.getEffectivePower());
    }

    @Test
    @DisplayName("Choosing both modes boosts the Vampire before it deals damage")
    void bothModesResolveInOrder() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());

        cast(new int[]{0, 1}, List.of(vampire.getId(), target.getId()));

        assertThat(vampire.getPowerModifier()).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(vampire.getEffectivePower());
    }

    @Test
    @DisplayName("The damage mode requires a Vampire you control and another creature")
    void damageModeRejectsIllegalTargets() {
        Permanent ownCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(ownCreature.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vampire you control");

        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        assertThatThrownBy(() -> cast(new int[]{1}, List.of(vampire.getId(), vampire.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The pump mode wears off at end of turn")
    void pumpModeExpiresAtEndOfTurn() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());

        cast(new int[]{0}, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isZero();
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        castWithoutResolving(modes, targetIds);
        harness.passBothPriorities();
    }

    @Test
    void pumpModeNeedsNoVampireOrTargets() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());

        cast(new int[]{0}, List.of());

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void damageCanTargetAnotherCreatureYouControl() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player1, new DawnhartDisciple());

        cast(new int[]{1}, List.of(vampire.getId(), target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(vampire.getPowerModifier()).isZero();
        resolveAllTriggers();
    }

    @Test
    void opponentVampireCannotBeDamageSource() {
        Permanent vampire = addCreatureReady(player2, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player1, new DawnhartDisciple());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(vampire.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pumpStillResolvesWhenDamageVictimLeaves() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());
        castWithoutResolving(new int[]{0, 1}, List.of(vampire.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(1);
        assertThat(vampire.getMarkedDamage()).isZero();
    }

    @Test
    void bothModesDoNothingWhenAllTargetsLeave() {
        Permanent vampire = addCreatureReady(player1, new VoldarenBloodcaster());
        Permanent target = addCreatureReady(player2, new DawnhartDisciple());
        Permanent other = addCreatureReady(player1, new DawnhartDisciple());
        castWithoutResolving(new int[]{0, 1}, List.of(vampire.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotGetPump() {
        cast(new int[]{0}, List.of());

        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());

        assertThat(creature.getPowerModifier()).isZero();
    }

    private void castWithoutResolving(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new MarkovRetribution()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
    }
}
