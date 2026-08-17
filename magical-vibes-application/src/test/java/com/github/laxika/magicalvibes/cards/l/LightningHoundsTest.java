package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningHoundsTest extends BaseCardTest {

    @Test
    @DisplayName("Lightning Hounds has first strike on the battlefield")
    void hasFirstStrikeOnBattlefield() {
        harness.addToBattlefield(player1, new LightningHounds());

        Permanent hounds = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hounds.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike defeats a smaller blocker before regular combat damage")
    void firstStrikeDefeatsSmallerBlocker() {
        Permanent attacker = new Permanent(new LightningHounds());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Hounds");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
