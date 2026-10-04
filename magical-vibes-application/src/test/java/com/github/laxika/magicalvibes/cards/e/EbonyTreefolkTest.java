package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbonyTreefolk.class})
class EbonyTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B}{G} gives Ebony Treefolk +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent treefolk = addReadyTreefolk(player1);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple activations give Ebony Treefolk a cumulative boost")
    void activatedAbilityStacks() {
        Permanent treefolk = addReadyTreefolk(player1);
        addBlackGreenMana(player1);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ebony Treefolk's temporary boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent treefolk = addReadyTreefolk(player1);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ebony Treefolk can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent treefolk = addReadyTreefolk(player1);
        treefolk.tap();
        treefolk.setSummoningSick(true);
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(4);
        assertThat(treefolk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability boosts only its source, even when other Ebony Treefolk are present")
    void boostsOnlySource() {
        Permanent treefolk = addReadyTreefolk(player1);
        Permanent other = addCreatureReady(player1, new EbonyTreefolk());
        Permanent opposing = addCreatureReady(player2, new EbonyTreefolk());
        addBlackGreenMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two black mana cannot replace the required green mana")
    void requiresGreenMana() {
        Permanent treefolk = addReadyTreefolk(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two green mana cannot replace the required black mana")
    void requiresBlackMana() {
        Permanent treefolk = addReadyTreefolk(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treefolk)).isEqualTo(3);
    }

    private Permanent addReadyTreefolk(Player player) {
        Permanent permanent = addCreatureReady(player, new EbonyTreefolk());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void addBlackGreenMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
