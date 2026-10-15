package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunhomeFortressOfTheLegion.class, BorosRecruit.class, BorosSignet.class})
class SunhomeFortressOfTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new SunhomeFortressOfTheLegion());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability grants target creature double strike")
    void grantsDoubleStrikeToTargetCreature() {
        harness.addToBattlefield(player1, new SunhomeFortressOfTheLegion());
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, recruit.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Double strike granted by the ability wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SunhomeFortressOfTheLegion());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, recruit.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SunhomeFortressOfTheLegion());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Double strike ability uses the stack and grants nothing before resolution")
    void doubleStrikeAbilityUsesTheStack() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SunhomeFortressOfTheLegion());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        addAbilityMana();

        harness.activateAbility(player1, 0, 1, null, recruit.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A land tapped for mana cannot also pay the double strike ability's tap cost")
    void cannotActivateDoubleStrikeAfterTappingForMana() {
        harness.addToBattlefield(player1, new SunhomeFortressOfTheLegion());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, recruit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Colorless mana cannot replace the red and white activation costs")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SunhomeFortressOfTheLegion());
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, recruit.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
