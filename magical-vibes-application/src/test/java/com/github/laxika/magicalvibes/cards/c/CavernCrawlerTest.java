package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavernCrawler.class, Mountain.class})
class CavernCrawlerTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsPowerAndReducesToughness() {
        Permanent crawler = addCrawlerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(2);
    }

    @Test
    void activatedAbilityCanBeUsedMultipleTimes() {
        Permanent crawler = addCrawlerReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
    }

    @Test
    void activatedAbilityRequiresRedMana() {
        Permanent crawler = addCrawlerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, crawler)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    void activatedAbilityCanBeUsedWhileSummoningSick() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new CavernCrawler());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(crawler.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(2);
    }

    @Test
    void activatedAbilityExpiresAtEndOfTurn() {
        Permanent crawler = addCrawlerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crawler)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    void mountainwalkPreventsBlockingWhenDefendingPlayerControlsMountain() {
        Permanent attacker = addCrawlerReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCrawlerReady(player2);
        harness.addToBattlefield(player2, new Mountain());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void mountainwalkAllowsBlockingWhenDefendingPlayerControlsNoMountain() {
        Permanent attacker = addCrawlerReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCrawlerReady(player2);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void mountainwalkIgnoresMountainsControlledByAttackingPlayer() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent attacker = addCrawlerReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addCrawlerReady(player2);

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addCrawlerReady(Player player) {
        return addCreatureReady(player, new CavernCrawler());
    }
}
