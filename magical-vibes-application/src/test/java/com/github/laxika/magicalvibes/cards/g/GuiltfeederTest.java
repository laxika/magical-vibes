package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Guiltfeeder.class, BlackKnight.class, Forest.class, HillGiant.class})
class GuiltfeederTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new Guiltfeeder());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unblocked Guiltfeeder makes the defending player lose life equal to their graveyard size")
    void unblockedLifeLossEqualsDefendingGraveyardSize() {
        harness.setGraveyard(player1, List.of(new Forest(), new HillGiant()));
        harness.setGraveyard(player2, List.of(new Forest(), new HillGiant(), new Forest()));
        addAttacker();

        int startingLife = gd.getLife(player2.getId());
        declareBlockers(List.of());

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Guiltfeeder does not trigger")
    void blockedDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new Forest(), new HillGiant()));
        Permanent attacker = addAttacker();
        Permanent blocker = new Permanent(new BlackKnight());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }
}
