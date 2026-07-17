package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HomaridWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants shroud, taps itself, and skips next untap")
    void activatingGrantsShroudTapsAndSkipsUntap() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(warrior.getGrantedKeywords()).contains(Keyword.SHROUD);
        assertThat(warrior.isTapped()).isTrue();
        assertThat(warrior.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new HomaridWarrior());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(warrior.getGrantedKeywords()).contains(Keyword.SHROUD);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getGrantedKeywords()).doesNotContain(Keyword.SHROUD);
    }
}
