package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSharpshooter.class, LlanowarElves.class})
class GoblinSharpshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringControllerUntapStep() {
        Permanent shooter = addReadyShooter(player1);
        shooter.tap();

        harness.performUntapStep(player1);

        assertThat(shooter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void dealsOneDamageToTargetPlayer() {
        addReadyShooter(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Untaps whenever a creature dies")
    void untapsWhenCreatureDies() {
        Permanent shooter = addReadyShooter(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shooter.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private Permanent addReadyShooter(Player player) {
        Permanent permanent = new Permanent(new GoblinSharpshooter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
