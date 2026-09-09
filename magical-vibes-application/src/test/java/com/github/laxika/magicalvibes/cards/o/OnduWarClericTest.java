package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnduWarCleric.class, HadaFreeblade.class})
class OnduWarClericTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and gains 2 life")
    void cohortTapsAnAllyAndGainsLife() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(cleric.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent cleric = addCreatureReady(player1, new OnduWarCleric());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(cleric), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
