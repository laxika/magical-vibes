package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MephiticOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact its controller controls")
    void getsPowerForControlledArtifacts() {
        Permanent ooze = addReadyCreature(player1, new MephiticOoze());

        assertThat(gqs.getEffectivePower(gd, ooze)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(5);

        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroys a creature it deals combat damage to without allowing regeneration")
    void destroysCreatureDealtCombatDamage() {
        Permanent ooze = addReadyCreature(player1, new MephiticOoze());
        harness.addToBattlefield(player1, new Ornithopter());
        ooze.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
