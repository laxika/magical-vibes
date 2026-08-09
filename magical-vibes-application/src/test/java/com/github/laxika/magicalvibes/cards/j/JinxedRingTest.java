package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JinxedRingTest extends BaseCardTest {

    @Test
    @DisplayName("Jinxed Ring deals 1 damage when a nontoken permanent enters your graveyard")
    void damagesControllerForOwnNontokenPermanent() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelsFeather());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.sacrificePermanent(player1, 1, harness.getPermanentId(player2, "Angel's Feather"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Aura of Silence");
    }

    @Test
    @DisplayName("Jinxed Ring ignores nontoken permanents put into an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelsFeather());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.sacrificePermanent(player2, 0, harness.getPermanentId(player2, "Angel's Feather"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Sacrifice a creature to give Jinxed Ring to an opponent")
    void sacrificeCreatureGivesRingToOpponent() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Jinxed Ring");
        harness.assertNotOnBattlefield(player1, "Jinxed Ring");
    }

    @Test
    @DisplayName("Cannot activate Jinxed Ring without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new JinxedRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
