package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SokenzanBruiser.class, Mountain.class, WanderingOnes.class})
class SokenzanBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Sokenzan Bruiser cannot be blocked when defending player controls a Mountain")
    void mountainwalkPreventsBlockingWithDefendingMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new WanderingOnes());
        Permanent bruiser = addAttackingBruiser();

        assertThatThrownBy(() -> declareBlock(blocker, bruiser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Sokenzan Bruiser can be blocked when defending player controls no Mountain")
    void mountainwalkAllowsBlockingWithoutDefendingMountain() {
        Permanent blocker = addCreatureReady(player2, new WanderingOnes());
        Permanent bruiser = addAttackingBruiser();

        declareBlock(blocker, bruiser);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sokenzan Bruiser's mountainwalk ignores a Mountain controlled by the attacking player")
    void mountainwalkChecksDefendingPlayerOnly() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new WanderingOnes());
        Permanent bruiser = addAttackingBruiser();

        declareBlock(blocker, bruiser);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingBruiser() {
        Permanent bruiser = addCreatureReady(player1, new SokenzanBruiser());
        bruiser.setAttacking(true);
        return bruiser;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
