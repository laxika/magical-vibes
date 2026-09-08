package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DryadSophisticate.class, DimirGuildgate.class, Forest.class, GrizzlyBears.class})
class DryadSophisticateTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked while defending player controls a nonbasic land")
    void cannotBeBlockedWithNonbasicLand() {
        harness.addToBattlefield(player2, new DimirGuildgate());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent attacker = addReadyAttacker(player1);

        prepareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player controls only basic lands")
    void canBeBlockedWithBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent attacker = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked when defending player controls no lands")
    void canBeBlockedWithoutLand() {
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent attacker = addReadyAttacker(player1);

        prepareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = new Permanent(new DryadSophisticate());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
