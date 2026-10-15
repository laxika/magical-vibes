package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyknightVanguard.class, Shock.class})
class SkyknightVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 1/1 Soldier token tapped and attacking")
    void attackCreatesSoldierToken() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new SkyknightVanguard());
        vanguard.setSummoningSick(false);

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
        assertThat(soldiers.getFirst().isAttacking()).isTrue();
        assertThat(soldiers.getFirst().isAttackedThisTurn()).isFalse();
        assertThat(soldiers.getFirst().getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Not attacking does not create a Soldier token")
    void noTriggerWithoutAttacking() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new SkyknightVanguard());
        vanguard.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"));
    }

    @Test
    @DisplayName("Each attacking Vanguard creates its own Soldier")
    void multipleVanguardsCreateSeparateTokens() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkyknightVanguard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkyknightVanguard());
        first.setSummoningSick(false);
        second.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({SkyknightVanguard.class, Shock.class})
    @DisplayName("Attack trigger still creates a Soldier after Vanguard is destroyed")
    void triggerResolvesAfterVanguardDies() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new SkyknightVanguard());
        vanguard.setSummoningSick(false);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.castInstant(player2, 0, vanguard.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Skyknight Vanguard");
        harness.assertNotOnBattlefield(player1, "Skyknight Vanguard");
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttacking()).isTrue();
        assertThat(tokens.getFirst().getAttackTarget()).isEqualTo(player2.getId());
    }
}
