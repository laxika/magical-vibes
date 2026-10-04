package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GhostflameSliver;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FirewakeSliver.class, GhostflameSliver.class, AshcoatBear.class})
class FirewakeSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Sliver creatures gain haste, including opposing Slivers")
    void grantsHasteToAllSlivers() {
        Permanent firewake = addCreatureReady(player1, new FirewakeSliver());
        Permanent opposingSliver = addCreatureReady(player2, new GhostflameSliver());
        Permanent bears = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, firewake, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A Sliver can sacrifice itself to give a target Sliver +2/+2")
    void sacrificesSourceAndBoostsTargetSliver() {
        Permanent firewake = addCreatureReady(player1, new FirewakeSliver());
        Permanent target = addCreatureReady(player2, new GhostflameSliver());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firewake);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firewake.getCard());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("The temporary Sliver boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FirewakeSliver());
        Permanent target = addCreatureReady(player1, new GhostflameSliver());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Sliver creature")
    void cannotTargetNonSliver() {
        addCreatureReady(player1, new FirewakeSliver());
        Permanent bears = addCreatureReady(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing Sliver sacrifices itself, not the ability-granting Firewake")
    void opposingSliverCanUseGrantedAbility() {
        Permanent firewake = addCreatureReady(player1, new FirewakeSliver());
        Permanent opposingSliver = addCreatureReady(player2, new GhostflameSliver());
        int basePower = gqs.getEffectivePower(gd, firewake);
        int baseToughness = gqs.getEffectiveToughness(gd, firewake);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, firewake.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSliver.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firewake);
        assertThat(gqs.getEffectivePower(gd, firewake)).isEqualTo(basePower);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firewake)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, firewake)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Sacrificing Firewake immediately removes its grant while its ability is pending")
    void grantEndsImmediatelyWhenFirewakeIsSacrificed() {
        Permanent firewake = addCreatureReady(player1, new FirewakeSliver());
        Permanent sliver = addCreatureReady(player1, new GhostflameSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, sliver.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firewake);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sliver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sliver);
    }

    @Test
    @DisplayName("Firewake does not grant abilities while it is a creature spell on the stack")
    void noAbilityGrantedBeforeFirewakeResolves() {
        Permanent sliver = addCreatureReady(player1, new GhostflameSliver());
        harness.setHand(player1, List.of(new FirewakeSliver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sliver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sliver);
    }

    @Test
    @DisplayName("A Sliver may target itself even though sacrificing it makes the target illegal")
    void canTargetTheSacrificedSource() {
        Permanent firewake = addCreatureReady(player1, new FirewakeSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, firewake.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firewake);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firewake.getCard());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
