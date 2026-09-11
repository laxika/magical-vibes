package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZadasCommando.class, HadaFreeblade.class, GrizzlyBears.class})
class ZadasCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and deals 1 damage to an opponent")
    void cohortDealsDamageToOpponent() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(commando.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally")
    void cohortRequiresAnotherUntappedAlly() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(commando.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cohort cannot target its controller")
    void cohortCannotTargetController() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(commando.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
