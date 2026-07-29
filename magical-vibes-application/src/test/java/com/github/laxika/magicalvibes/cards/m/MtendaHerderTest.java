package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class MtendaHerderTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void blockerWithoutFlankingGetsMinusOneMinusOne() {
        Permanent herder = addReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mtenda Herder");
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A 1/1 blocker without flanking dies to the flanking trigger")
    void oneOneBlockerDies() {
        Permanent herder = addReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        addCreatureReady(player2, new FugitiveWizard());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("A blocker that also has flanking is unaffected")
    void blockerWithFlankingIsUnaffected() {
        Permanent herder = addReady(player1, new MtendaHerder());
        herder.setAttacking(true);
        addReady(player2, new MtendaHerder());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent herder = addReady(player1, new MtendaHerder());
        herder.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
