package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Brainwash;
import com.github.laxika.magicalvibes.cards.g.GoblinCaves;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiracleWorker.class, Brainwash.class, GoblinCaves.class, MazeOfIth.class, Squire.class})
class MiracleWorkerTest extends BaseCardTest {

    @Test
    void destroysAuraAttachedToCreature() {
        addReadyMiracleWorker(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Brainwash());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Brainwash");
        harness.assertInGraveyard(player2, "Brainwash");
    }

    @Test
    void cannotTargetAuraAttachedToOpponentCreature() {
        addReadyMiracleWorker(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Brainwash());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature");
    }

    @Test
    void cannotTargetAuraAttachedToLand() {
        addReadyMiracleWorker(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MazeOfIth());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GoblinCaves());
        aura.setAttachedTo(land.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature");
    }

    @Test
    void destroysAuraYouControlAttachedToMiracleWorker() {
        Permanent worker = addReadyMiracleWorker(player1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Brainwash());
        aura.setAttachedTo(worker.getId());

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brainwash");
        harness.assertInGraveyard(player1, "Brainwash");
        harness.assertOnBattlefield(player1, "Miracle Worker");
    }

    @Test
    void cannotTargetCreatureItself() {
        addReadyMiracleWorker(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a creature");
    }

    @Test
    void doesNotDestroyAuraIfEnchantedCreatureChangesControllerBeforeResolution() {
        addReadyMiracleWorker(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Squire());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Brainwash());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, aura.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Brainwash");
    }

    private Permanent addReadyMiracleWorker(Player player) {
        return addCreatureReady(player, new MiracleWorker());
    }
}
