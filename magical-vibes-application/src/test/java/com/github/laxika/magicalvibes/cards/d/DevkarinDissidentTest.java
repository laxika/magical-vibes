package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DevkarinDissidentTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +2/+2 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dissident.getPowerModifier()).isEqualTo(2);
        assertThat(dissident.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability can be used repeatedly")
    void activatedAbilityBoostsSelfRepeatedly() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dissident.getPowerModifier()).isEqualTo(4);
        assertThat(dissident.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability boost wears off at end of turn")
    void activatedAbilityBoostResetsAtEndOfTurn() {
        Permanent dissident = addReadyDissident();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dissident.getPowerModifier()).isZero();
        assertThat(dissident.getToughnessModifier()).isZero();
    }

    private Permanent addReadyDissident() {
        Permanent permanent = new Permanent(new DevkarinDissident());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
