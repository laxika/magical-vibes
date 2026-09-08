package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StromgaldCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Stromgald Crusader has protection from white")
    void hasProtectionFromWhite() {
        Permanent crusader = addCrusaderReady(player1);

        assertThat(gqs.hasProtectionFrom(gd, crusader, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, crusader, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Resolving the first ability grants flying until end of turn")
    void firstAbilityGrantsFlying() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by the first ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the flying ability without black mana")
    void cannotActivateFlyingWithoutMana() {
        addCrusaderReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving the second ability gives +1/+0 until end of turn")
    void secondAbilityBoostsPower() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost granted by the second ability resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the boost ability with only one black mana")
    void cannotActivateBoostWithoutEnoughMana() {
        addCrusaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addCrusaderReady(Player player) {
        Permanent permanent = new Permanent(new StromgaldCrusader());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
