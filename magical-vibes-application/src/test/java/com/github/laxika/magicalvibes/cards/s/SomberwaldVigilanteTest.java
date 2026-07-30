package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SomberwaldVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 1 damage to the blocker")
    void becomingBlockedDeals1DamageToBlocker() {
        Permanent vigilante = addReadyVigilante(player1);
        vigilante.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(vigilante.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent damagedBlocker = findPermanent(player2, "Grizzly Bears");
        assertThat(damagedBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures fires once per blocker")
    void becomingBlockedByTwoCreaturesFiresPerBlocker() {
        Permanent vigilante = addReadyVigilante(player1);
        vigilante.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player2, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(p -> p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Blocking does not trigger the ability")
    void blockingDoesNotTrigger() {
        addReadyVigilante(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addReadyVigilante(Player player) {
        Permanent perm = new Permanent(new SomberwaldVigilante());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
