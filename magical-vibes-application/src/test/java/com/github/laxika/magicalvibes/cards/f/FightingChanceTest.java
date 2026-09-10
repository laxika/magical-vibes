package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CinderCrawler;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FightingChance.class, FurnaceBrood.class, CinderCrawler.class, HighGround.class})
class FightingChanceTest extends BaseCardTest {

    @Test
    @DisplayName("Flips independently for each blocking creature and prevents winning blockers' damage")
    void flipsForEachBlockingCreature() {
        Permanent firstAttacker = addCreatureReady(player1, new FurnaceBrood());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new FurnaceBrood());
        secondAttacker.setAttacking(true);
        addCreatureReady(player2, new CinderCrawler());
        addCreatureReady(player2, new CinderCrawler());
        prepareSpell();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<String> flips = fightingChanceFlipLogs();
        long losses = flips.stream().filter(log -> log.contains(" loses the coin flip")).count();
        assertThat(flips).hasSize(2);

        resolveCombat();

        assertThat(firstAttacker.getMarkedDamage() + secondAttacker.getMarkedDamage())
                .isEqualTo(losses);
    }

    @Test
    @DisplayName("Flips once when one blocking creature blocks multiple attackers")
    void flipsOncePerBlockingCreature() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent firstAttacker = addCreatureReady(player1, new FurnaceBrood());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new FurnaceBrood());
        secondAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CinderCrawler());
        prepareSpell();

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(fightingChanceFlipLogs()).hasSize(1);
    }

    @Test
    @DisplayName("A combat with no blockers produces no coin flips")
    void noBlockersNoFlips() {
        addCreatureReady(player1, new FurnaceBrood()).setAttacking(true);
        prepareSpell();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Fighting Chance")).isFalse();
    }

    private List<String> fightingChanceFlipLogs() {
        return gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Fighting Chance"))
                .toList();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FightingChance()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
