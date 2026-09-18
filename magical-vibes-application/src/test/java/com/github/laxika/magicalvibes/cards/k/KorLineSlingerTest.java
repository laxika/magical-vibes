package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorLineSlinger.class, EnormousBaloth.class, HillGiant.class})
class KorLineSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature with power 3 or less")
    void tapsLowPowerCreature() {
        addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Kor Line-Slinger")
    void activatingTapsSelf() {
        Permanent lineSlinger = addCreatureReady(player1, new KorLineSlinger());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(lineSlinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than 3 is an illegal target")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new KorLineSlinger());
        Permanent giant = addCreatureReady(player2, new EnormousBaloth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
