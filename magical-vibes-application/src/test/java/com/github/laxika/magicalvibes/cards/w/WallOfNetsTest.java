package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfNets.class, HighGround.class, RagingGoblin.class})
class WallOfNetsTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat, Wall of Nets exiles the creature it blocked")
    void exilesBlockedCreatureAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
    }

    @Test
    @DisplayName("A creature not blocked by Wall of Nets is not exiled")
    void doesNotExileUnblockedCreature() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("At end of combat, Wall of Nets exiles every creature it blocked")
    void exilesEveryCreatureItBlockedAtEndOfCombat() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent wall = addCreatureReady(player2, new WallOfNets());

        Permanent attacker1 = addCreatureReady(player1, new RagingGoblin());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new RagingGoblin());
        attacker2.setAttacking(true);

        List<Permanent> attackerBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderBattlefield.indexOf(wall), attackerBattlefield.indexOf(attacker1)),
                new BlockerAssignment(defenderBattlefield.indexOf(wall), attackerBattlefield.indexOf(attacker2))));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker1.getOriginalCard()
                        || permanent.getOriginalCard() == attacker2.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(attacker1.getOriginalCard(), attacker2.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
    }

    @Test
    @DisplayName("Creatures exiled by Wall of Nets return under their owners' control when it leaves")
    void returnsExiledCreatureWhenWallLeaves() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("A creature returns under its owner's control when Wall of Nets leaves")
    void returnsStolenCreatureToItsOwnerWhenWallLeaves() {
        Permanent attacker = addCreatureReady(player2, new RagingGoblin());
        attacker.setAttacking(true);
        gd.stolenCreatures.put(attacker.getId(), player1.getId());
        Permanent wall = addCreatureReady(player1, new WallOfNets());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(attacker.getOriginalCard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("Wall of Nets leaving before end of combat prevents its exile trigger")
    void leavingBeforeEndOfCombatPreventsExile() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("Wall of Nets still exiles its blocked creature if it leaves after triggering")
    void triggerStillExilesBlockedCreatureAfterWallLeaves() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfNets());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, wall));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == attacker.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getOriginalCard());
    }
}
