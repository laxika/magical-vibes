package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmeltWardIgnusTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Smelt-Ward Ignus temporarily steals a creature with power 3 or less")
    void sacrificesAndStealsTarget() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.tap();
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Smelt-Ward Ignus");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).contains(target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).doesNotContain(target.getId());
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at the end of the turn")
    void controlAndHasteExpireAtEndOfTurn() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new HillGiant());
        prepareForSorcerySpeedActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).contains(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).doesNotContain(target.getId());
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetCreatureWithPowerGreaterThanThree() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new SerraAngel());
        prepareForSorcerySpeedActivation();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void requiresSorcerySpeed() {
        addCreatureReady(player1, new SmeltWardIgnus());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void prepareForSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
