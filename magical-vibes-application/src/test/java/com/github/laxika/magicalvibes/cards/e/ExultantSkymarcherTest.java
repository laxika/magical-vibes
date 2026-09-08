package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExultantSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("Exultant Skymarcher can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent skymarcher = new Permanent(new ExultantSkymarcher());
        skymarcher.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(skymarcher);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(skymarcher.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Exultant Skymarcher can block a creature without flying")
    void canBlockNonFlyingCreature() {
        Permanent skymarcher = new Permanent(new ExultantSkymarcher());
        skymarcher.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(skymarcher);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(skymarcher.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying cannot block Exultant Skymarcher")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent skymarcher = new Permanent(new ExultantSkymarcher());
        skymarcher.setSummoningSick(false);
        skymarcher.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(skymarcher);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
