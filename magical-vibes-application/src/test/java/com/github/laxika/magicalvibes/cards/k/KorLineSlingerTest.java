package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KorLineSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature with power 3 or less")
    void tapsLowPowerCreature() {
        addReady(new KorLineSlinger(), player1);
        Permanent target = addReady(new HillGiant(), player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Kor Line-Slinger")
    void activatingTapsSelf() {
        Permanent lineSlinger = addReady(new KorLineSlinger(), player1);
        Permanent target = addReady(new HillGiant(), player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(lineSlinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with power greater than 3 is an illegal target")
    void cannotTargetHighPowerCreature() {
        addReady(new KorLineSlinger(), player1);
        Permanent giant = addReady(new EnormousBaloth(), player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Card card, Player player) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
