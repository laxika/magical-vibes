package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        harness.setHand(player1, List.of(new GoblinBerserker()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        GameService gs = harness.getGameService();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        Permanent berserker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(berserker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("First strike kills an equally sized blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent berserker = new Permanent(new GoblinBerserker());
        berserker.setSummoningSick(false);
        berserker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(berserker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Berserker");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
