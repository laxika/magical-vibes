package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasEmbrace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LushGrowth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiracleWorker.class, GaeasEmbrace.class, GrizzlyBears.class, LushGrowth.class, Forest.class})
class MiracleWorkerTest extends BaseCardTest {

    @Test
    void destroysAuraAttachedToCreature() {
        addReadyMiracleWorker(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GaeasEmbrace());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gaea's Embrace");
        harness.assertInGraveyard(player2, "Gaea's Embrace");
    }

    @Test
    void cannotTargetAuraAttachedToLand() {
        addReadyMiracleWorker(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LushGrowth());
        aura.setAttachedTo(land.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature");
    }

    private Permanent addReadyMiracleWorker(Player player) {
        Permanent worker = harness.addToBattlefieldAndReturn(player, new MiracleWorker());
        worker.setSummoningSick(false);
        return worker;
    }
}
