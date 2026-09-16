package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.e.ElvishPioneer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSharpshooter.class, BarkhideMauler.class, ElvishPioneer.class})
class GoblinSharpshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringControllerUntapStep() {
        Permanent shooter = addCreatureReady(player1, new GoblinSharpshooter());
        shooter.tap();

        harness.performUntapStep(player1);

        assertThat(shooter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void dealsOneDamageToTargetPlayer() {
        Permanent shooter = addCreatureReady(player1, new GoblinSharpshooter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shooter.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature without killing a 4/4")
    void dealsOneDamageToTargetCreature() {
        Permanent shooter = addCreatureReady(player1, new GoblinSharpshooter());
        harness.addToBattlefield(player2, new BarkhideMauler());
        Permanent target = findPermanent(player2, "Barkhide Mauler");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(shooter.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Barkhide Mauler");
    }

    @Test
    @DisplayName("Untaps whenever a creature dies")
    void untapsWhenCreatureDies() {
        Permanent shooter = addCreatureReady(player1, new GoblinSharpshooter());
        harness.addToBattlefield(player2, new ElvishPioneer());

        UUID targetId = findPermanent(player2, "Elvish Pioneer").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shooter.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Elvish Pioneer");
    }
}
