package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredWolf.class, LightningBolt.class, ProdigalPyromancer.class, DayOfJudgment.class})
class SacredWolfTest extends BaseCardTest {

    @Test
    void opponentCannotTargetWithSpell() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SacredWolf());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, wolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sacred Wolf");
    }

    @Test
    void controllerCanTargetWithSpell() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SacredWolf());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sacred Wolf");
        harness.assertInGraveyard(player1, "Sacred Wolf");
    }

    @Test
    void opponentCannotTargetWithActivatedAbility() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SacredWolf());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, wolf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        assertThat(pyromancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sacred Wolf");
    }

    @Test
    void controllerCanTargetWithActivatedAbility() {
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new SacredWolf());

        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sacred Wolf");
        harness.assertInGraveyard(player1, "Sacred Wolf");
    }

    @Test
    void hexproofDoesNotPreventUntargetedDestruction() {
        harness.addToBattlefield(player1, new SacredWolf());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DayOfJudgment(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sacred Wolf");
        harness.assertInGraveyard(player1, "Sacred Wolf");
    }
}
