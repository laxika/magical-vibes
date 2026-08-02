package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MantaRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Manta Riders has no flying by default")
    void noFlyingByDefault() {
        Permanent riders = addReadyRiders(player1);

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("{U}: Manta Riders gains flying until end of turn")
    void activationGrantsFlying() {
        Permanent riders = addReadyRiders(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent riders = addReadyRiders(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, riders, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyRiders(Player player) {
        Permanent perm = new Permanent(new MantaRiders());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
