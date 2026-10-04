package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KragmaButcher;
import com.github.laxika.magicalvibes.cards.p.PainSeer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FelhideBrawler.class, KragmaButcher.class, PainSeer.class})
class FelhideBrawlerTest extends BaseCardTest {

    @Test
    void cannotBlockWithoutAnotherMinotaur() {
        Permanent brawler = addCreatureReady(player2, new FelhideBrawler());
        Permanent attacker = addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(brawler);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithAnotherMinotaur() {
        Permanent brawler = addCreatureReady(player2, new FelhideBrawler());
        addCreatureReady(player2, new KragmaButcher());
        Permanent attacker = addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(brawler);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(brawler.isBlocking()).isTrue();
    }

    @Test
    void opponentControllingAnotherMinotaurDoesNotAllowBlocking() {
        Permanent brawler = addCreatureReady(player2, new FelhideBrawler());
        addCreatureReady(player1, new KragmaButcher());
        Permanent attacker = addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(1));

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(brawler);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anotherFelhideBrawlerAllowsBothToBlock() {
        Permanent first = addCreatureReady(player2, new FelhideBrawler());
        Permanent second = addCreatureReady(player2, new FelhideBrawler());
        addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void tappedSummoningSickMinotaurStillAllowsBlocking() {
        Permanent brawler = addCreatureReady(player2, new FelhideBrawler());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new KragmaButcher());
        other.tap();
        other.setSummoningSick(true);
        addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(brawler.isBlocking()).isTrue();
    }

    @Test
    void anotherNonMinotaurDoesNotAllowBlocking() {
        addCreatureReady(player2, new FelhideBrawler());
        addCreatureReady(player2, new PainSeer());
        addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minotaurThatLeftBattlefieldDoesNotAllowBlocking() {
        addCreatureReady(player2, new FelhideBrawler());
        Permanent other = addCreatureReady(player2, new KragmaButcher());
        addCreatureReady(player1, new PainSeer());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithoutAnotherMinotaur() {
        Permanent brawler = addCreatureReady(player1, new FelhideBrawler());
        addCreatureReady(player2, new PainSeer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(brawler.isAttacking()).isTrue();
    }
}
