package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyknightVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 Soldier token tapped and attacking")
    void attackCreatesSoldierToken() {
        Permanent vanguard = new Permanent(new SkyknightVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vanguard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .toList();
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().isTapped()).isTrue();
        assertThat(soldiers.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Not attacking does not create a Soldier token")
    void noTriggerWithoutAttacking() {
        Permanent vanguard = new Permanent(new SkyknightVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vanguard);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"));
    }
}
