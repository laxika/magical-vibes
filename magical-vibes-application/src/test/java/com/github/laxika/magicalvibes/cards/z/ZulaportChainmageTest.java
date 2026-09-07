package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZulaportChainmage.class, HadaFreeblade.class})
class ZulaportChainmageTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and makes an opponent lose 2 life")
    void cohortMakesOpponentLoseLife() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(chainmage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally and an opponent target")
    void cohortRequiresUntappedAllyAndOpponentTarget() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(chainmage.isTapped()).isFalse();

        addCreatureReady(player1, new HadaFreeblade());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(chainmage.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
