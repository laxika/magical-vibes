package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoFangtail.class, BorosRecruit.class})
class ViashinoFangtailTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(fangtail.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addCreatureReady(player1, new ViashinoFangtail());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Deals 1 damage to a 3/3 creature without destroying it")
    void deals1DamageToCreatureThatSurvives() {
        addCreatureReady(player1, new ViashinoFangtail());
        Permanent target = addCreatureReady(player2, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Viashino Fangtail");
    }

    @Test
    @DisplayName("Ability fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetCreatureLeavesBeforeResolution() {
        addCreatureReady(player1, new ViashinoFangtail());
        Permanent target = addCreatureReady(player2, new BorosRecruit());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void dealsDamageToController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, fangtail.getId());
        harness.passBothPriorities();

        assertThat(fangtail.isTapped()).isTrue();
        assertThat(fangtail.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Viashino Fangtail");
    }

    @Test
    @DisplayName("Ability resolves even after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(fangtail);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new ViashinoFangtail());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent fangtail = addCreatureReady(player1, new ViashinoFangtail());
        fangtail.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

}
