package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
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

class RockBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("When Rock Basilisk becomes blocked by a non-Wall creature, that creature is scheduled for end-of-combat destruction")
    void becomesBlockedByNonWallSchedulesDestruction() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        Permanent avatar = addReadyAvatar(player2); // non-Wall

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Rock Basilisk")
                        && se.getTargetId().equals(avatar.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(avatar.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        addReadyAvatar(player2); // 8/8 survives Basilisk's 4 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
    }

    @Test
    @DisplayName("When Rock Basilisk becomes blocked by a Wall creature, nothing is scheduled for destruction")
    void becomesBlockedByWallSchedulesNothing() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        addReadyWall(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When Rock Basilisk blocks a non-Wall creature, that attacker is scheduled for end-of-combat destruction")
    void blocksNonWallSchedulesDestruction() {
        Permanent attacker = addReadyAvatar(player1); // non-Wall
        attacker.setAttacking(true);
        addReadyBasilisk(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Rock Basilisk")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    private Permanent addReadyBasilisk(Player player) {
        Permanent perm = new Permanent(new RockBasilisk());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAvatar(Player player) {
        Permanent perm = new Permanent(new AvatarOfMight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfWood());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
