package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampartSmasher.class, YouthfulKnight.class, RovingKeep.class, SporecapSpider.class})
class RampartSmasherTest extends BaseCardTest {

    @Test
    @DisplayName("Rampart Smasher can't be blocked by a Knight")
    void cannotBeBlockedByKnight() {
        Permanent blocker = addReadyBlocker(new YouthfulKnight());
        Permanent attacker = addReadyAttacker();
        beginBlockerDeclaration();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rampart Smasher can't be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent blocker = addReadyBlocker(new RovingKeep());
        Permanent attacker = addReadyAttacker();
        beginBlockerDeclaration();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rampart Smasher can be blocked by a creature that is not a Knight or Wall")
    void canBeBlockedByOtherCreature() {
        Permanent blocker = addReadyBlocker(new SporecapSpider());
        Permanent attacker = addReadyAttacker();
        beginBlockerDeclaration();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rampart Smasher does not prevent Knights from blocking other attackers")
    void doesNotRestrictBlockingOtherAttackers() {
        harness.addToBattlefield(player1, new RampartSmasher());
        Permanent blocker = addReadyBlocker(new YouthfulKnight());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SporecapSpider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        beginBlockerDeclaration();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rampart Smasher can block an attacking Knight")
    void canBlockKnight() {
        Permanent blocker = addReadyBlocker(new RampartSmasher());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        beginBlockerDeclaration();

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyBlocker(Card card) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, card);
        blocker.setSummoningSick(false);
        return blocker;
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RampartSmasher());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
