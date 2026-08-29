package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KrakenOfTheStraitsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power less than the Island count can't block")
    void cannotBeBlockedByCreatureBelowIslandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        beginBlockerDeclaration();

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

        beginBlockerDeclaration();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction does not apply when its controller controls no Islands")
    void canBeBlockedWithoutIslands() {
        Permanent blocker = addCreatureReady(player2, new Ornithopter());
        Permanent kraken = addCreatureReady(player1, new KrakenOfTheStraits());
        kraken.setAttacking(true);

        beginBlockerDeclaration();
        declareBlock(blocker, kraken);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
