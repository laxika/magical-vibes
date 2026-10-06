package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrakenOfTheStraits.class, Island.class, FugitiveWizard.class, Ornithopter.class})
class KrakenOfTheStraitsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power less than the Island count can't block")
    void cannotBeBlockedByCreatureBelowIslandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, kraken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("A creature with power equal to the Island count can block")
    void canBeBlockedByCreatureEqualToIslandCount() {
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction does not apply when its controller controls no Islands")
    void canBeBlockedWithoutIslands() {
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The defending player's Islands do not increase the restriction")
    void defendingPlayersIslandsDoNotCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped Islands still count")
    void tappedIslandsCount() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, kraken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("A blocker whose power is increased to the Island count can block")
    void increasedEffectivePowerAllowsBlocking() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setPowerModifier(1);
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Negative-power creatures cannot block even with no Islands")
    void negativePowerCannotBlockWithoutIslands() {
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        blocker.setPowerModifier(-1);
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, kraken))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
