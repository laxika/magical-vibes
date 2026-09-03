package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RockBasilisk.class, AvatarOfMight.class, WallOfWood.class, DeepFreeze.class})
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
    @DisplayName("When Rock Basilisk becomes blocked by a Wall creature, its ability does not trigger")
    void becomesBlockedByWallDoesNotTriggerAbility() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        addReadyWall(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Rock Basilisk"));
    }

    @Test
    @DisplayName("When Rock Basilisk becomes blocked by two non-Wall creatures, each creature is scheduled for destruction")
    void becomesBlockedByTwoNonWallCreaturesSchedulesBoth() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        Permanent firstAvatar = addReadyAvatar(player2);
        Permanent secondAvatar = addReadyAvatar(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack)
                .filteredOn(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Rock Basilisk"))
                .extracting(se -> se.getTargetId())
                .containsExactlyInAnyOrder(firstAvatar.getId(), secondAvatar.getId());

        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(firstAvatar.getId()))
                .anyMatch(a -> a.permanentId().equals(secondAvatar.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker that becomes a Wall before the trigger resolves is still destroyed at end of combat")
    void nonWallBlockerThatBecomesWallBeforeResolutionIsDestroyed() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);
        Permanent avatar = addReadyAvatar(player2);
        avatar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent deepFreeze = new Permanent(new DeepFreeze());
        deepFreeze.setAttachedTo(avatar.getId());
        gd.playerBattlefields.get(player2.getId()).add(deepFreeze);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
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
        return addCreatureReady(player, new RockBasilisk());
    }

    private Permanent addReadyAvatar(Player player) {
        return addCreatureReady(player, new AvatarOfMight());
    }

    private Permanent addReadyWall(Player player) {
        return addCreatureReady(player, new WallOfWood());
    }
}
