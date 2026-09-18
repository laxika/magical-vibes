package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sparksmith.class, GoblinSkyRaider.class, GlorySeeker.class})
class SparksmithTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the Goblins on the battlefield and damages its controller")
    void dealsDamageEqualToBattlefieldGoblinsAndDamagesController() {
        harness.setLife(player1, 20);
        Permanent sparksmith = addCreatureReady(player1, new Sparksmith());
        harness.addToBattlefield(player2, new GoblinSkyRaider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glory Seeker");
        assertThat(sparksmith.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Counts Goblins when the ability resolves")
    void countsGoblinsAtResolution() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Sparksmith());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinSkyRaider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(goblin);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glory Seeker");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not count the source after it leaves before resolution")
    void doesNotCountSourceAfterLeavingBeforeResolution() {
        harness.setLife(player1, 20);
        Permanent sparksmith = addCreatureReady(player1, new Sparksmith());
        harness.addToBattlefield(player2, new GoblinSkyRaider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sparksmith);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glory Seeker");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new Sparksmith());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
