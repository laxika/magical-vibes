package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreadSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("When Dread Specter becomes blocked by a nonblack creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonblackSchedulesDestruction() {
        Permanent specter = addReadySpecter(player1);
        specter.setAttacking(true);
        Permanent spider = addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("A nonblack blocker survives combat damage but is destroyed at end of combat")
    void nonblackBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent specter = addReadySpecter(player1);
        specter.setAttacking(true);
        addReadySpider(player2); // 2/4 survives Dread Specter's 2 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Dread Specter becomes blocked by a black creature, nothing is scheduled for destruction")
    void becomesBlockedByBlackSchedulesNothing() {
        Permanent specter = addReadySpecter(player1);
        specter.setAttacking(true);
        addReadyVampire(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Dread Specter blocks a nonblack creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonblackSchedulesDestruction() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadySpecter(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Dread Specter")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When Dread Specter blocks a black creature, nothing is scheduled for destruction")
    void blocksBlackSchedulesNothing() {
        Permanent attacker = addReadyVampire(player1);
        attacker.setAttacking(true);
        addReadySpecter(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    private Permanent addReadySpecter(Player player) {
        Permanent perm = new Permanent(new DreadSpecter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyVampire(Player player) {
        Permanent perm = new Permanent(new VampireAristocrat());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
