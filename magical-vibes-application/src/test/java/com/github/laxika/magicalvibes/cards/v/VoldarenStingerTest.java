package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VoldarenStinger.class)
class VoldarenStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike only while attacking")
    void firstStrikeWhileAttacking() {
        Permanent stinger = addStinger();

        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isFalse();

        stinger.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isTrue();

        stinger.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, stinger, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Activation gives +2/+0 until end of turn")
    void activationBoostsSelf() {
        Permanent stinger = addStinger();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.passBothPriorities();

        assertThat(stinger.getPowerModifier()).isEqualTo(2);
        assertThat(stinger.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activation boost wears off at end of turn")
    void activationBoostWearsOffAtEndOfTurn() {
        Permanent stinger = addStinger();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(stinger), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stinger.getPowerModifier()).isEqualTo(0);
        assertThat(stinger.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addStinger() {
        return addCreatureReady(player1, new VoldarenStinger());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
