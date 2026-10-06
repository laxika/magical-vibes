package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HorizonScholar;
import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverbeakGriffin.class, OreskosSwiftclaw.class, HorizonScholar.class, GiantSpider.class})
class SilverbeakGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Silverbeak Griffin can't be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent blocker = addCreatureReady(player2, new OreskosSwiftclaw());
        Permanent attacker = addCreatureReady(player1, new SilverbeakGriffin());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Silverbeak Griffin can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent blocker = addCreatureReady(player2, new HorizonScholar());
        Permanent attacker = addCreatureReady(player1, new SilverbeakGriffin());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Silverbeak Griffin can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent attacker = addCreatureReady(player1, new SilverbeakGriffin());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Silverbeak Griffin can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        Permanent blocker = addCreatureReady(player2, new SilverbeakGriffin());
        Permanent attacker = addCreatureReady(player1, new OreskosSwiftclaw());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Silverbeak Griffin can block a creature with flying")
    void canBlockCreatureWithFlying() {
        Permanent blocker = addCreatureReady(player2, new SilverbeakGriffin());
        Permanent attacker = addCreatureReady(player1, new HorizonScholar());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
