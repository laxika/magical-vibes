package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartwoodGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest deals 2 damage to target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        readyGiant();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without a Forest to sacrifice")
    void cannotActivateWithoutForest() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Island());

        readyGiant();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature is not a legal target")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        readyGiant();
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new HeartwoodGiant());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyGiant() {
        Permanent giant = findPermanent(player1, "Heartwood Giant");
        giant.setSummoningSick(false);
    }
}
