package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamFighter.class, IronTuskElephant.class})
class DreamFighterTest extends BaseCardTest {

    @Test
    @DisplayName("When Dream Fighter blocks a creature, both it and the attacker phase out")
    void blocksPhasesOutBoth() {
        Permanent attacker = addCreatureReady(player1, new IronTuskElephant());
        attacker.setAttacking(true);
        Permanent fighter = addCreatureReady(player2, new DreamFighter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fighter);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(fighter);
    }

    @Test
    @DisplayName("When Dream Fighter becomes blocked, both it and the blocker phase out")
    void becomesBlockedPhasesOutBoth() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.findPermanentById(gd, fighter.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, blocker.getId())).isNull();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(fighter);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("When Dream Fighter becomes blocked by multiple creatures, it phases out with every blocker")
    void becomesBlockedByMultipleCreaturesPhasesOutEachBlocker() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new IronTuskElephant());
        Permanent secondBlocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fighter);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker)
                .doesNotContain(secondBlocker);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(fighter);
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .contains(firstBlocker, secondBlocker);
    }

    @Test
    @DisplayName("Phasing out removes both creatures from combat, so no combat damage is dealt")
    void phasedOutCreaturesDealNoCombatDamage() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(fighter.isAttacking()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();

        assertThat(fighter.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Each phased-out creature phases back in during its own controller's next untap step")
    void phasesBackInOnControllersNextUntapStep() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        advanceToUpkeep(player2); // player2's untap step — their blocker phases in
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fighter);

        advanceToUpkeep(player1); // player1's untap step — Dream Fighter phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fighter);
    }

}
