package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
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

@CardUsed({MagmaticChasm.class, DevilthornFox.class, StormriderSpirit.class})
class MagmaticChasmTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying can't block this turn")
    void nonFliersCantBlock() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        addCreatureReady(player2, new DevilthornFox());

        castMagmaticChasm();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures with flying can still block")
    void fliersCanBlock() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        Permanent blocker = addCreatureReady(player2, new StormriderSpirit());

        castMagmaticChasm();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction applies to both players' creatures")
    void affectsBothPlayers() {
        Permanent ownBears = addCreatureReady(player1, new DevilthornFox());
        Permanent opponentBears = addCreatureReady(player2, new DevilthornFox());

        castMagmaticChasm();

        assertThat(bls.canBlockAttacker(gd, ownBears, opponentBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, opponentBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Nonflying creatures entering after resolution cannot block")
    void laterNonFliersCantBlock() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());

        castMagmaticChasm();

        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new DevilthornFox());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Flying creatures entering after resolution can block")
    void laterFliersCanBlock() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());

        castMagmaticChasm();

        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new StormriderSpirit());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());

        castMagmaticChasm();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castMagmaticChasm() {
        harness.castFromHand(player1, new MagmaticChasm(), "{1}{R}");
        harness.passBothPriorities();
    }

}
