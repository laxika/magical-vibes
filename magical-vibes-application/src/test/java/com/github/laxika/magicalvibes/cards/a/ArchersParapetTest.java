package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchersParapetTest extends BaseCardTest {

    @Test
    void activationMakesEachOpponentLoseLifeAndTapsSource() {
        Permanent parapet = addReadyParapet();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(parapet.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        addReadyParapet();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyParapet() {
        Permanent parapet = new Permanent(new ArchersParapet());
        parapet.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(parapet);
        return parapet;
    }
}
