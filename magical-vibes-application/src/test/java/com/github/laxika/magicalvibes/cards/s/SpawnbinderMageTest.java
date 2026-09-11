package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawnbinderMage.class, HadaFreeblade.class, GrizzlyBears.class})
class SpawnbinderMageTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and the target creature")
    void cohortTapsAllyAndTargetCreature() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(mage.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
