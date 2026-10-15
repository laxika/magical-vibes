package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.k.KillerWhale;
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

@CardUsed({GuidelightOptimizer.class, CopperMyr.class, KillerWhale.class, GrizzlyBears.class})
class GuidelightOptimizerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Guidelight Optimizer adds blue artifact-spell-or-ability-restricted mana")
    void addsBlueArtifactSpellOrAbilityRestrictedMana() {
        addReadyOptimizer();

        harness.activateAbility(player1, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Guidelight mana can pay for an artifact spell")
    void restrictedManaCanPayForArtifactSpell() {
        addReadyOptimizer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Guidelight mana can pay for a nonartifact ability")
    void restrictedManaCanPayForNonartifactAbility() {
        addReadyOptimizer();
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new KillerWhale());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whale, Keyword.FLYING)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Guidelight mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifactSpell() {
        addReadyOptimizer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted blue mana pays the blue cost of an artifact spell")
    void restrictedManaPaysColoredArtifactCost() {
        addReadyOptimizer();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GuidelightOptimizer()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The mana ability resolves immediately and cannot be activated twice without untapping")
    void manaAbilityTapsSourceAndDoesNotUseStack() {
        addReadyOptimizer();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Guidelight Optimizer").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Optimizer cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        Permanent optimizer = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        optimizer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(optimizer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactSpellOrAbilityOnlyMana(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyOptimizer() {
        addCreatureReady(player1, new GuidelightOptimizer());
    }
}
