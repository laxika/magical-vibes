package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulmenderTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability gains 1 life and taps Soulmender")
    void abilityGainsLife() {
        Permanent soulmender = addReadySoulmender(player1);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(soulmender.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be activated while tapped")
    void cannotActivateWhileTapped() {
        Permanent soulmender = addReadySoulmender(player1);
        soulmender.tap();
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    private Permanent addReadySoulmender(Player player) {
        Permanent perm = new Permanent(new Soulmender());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
