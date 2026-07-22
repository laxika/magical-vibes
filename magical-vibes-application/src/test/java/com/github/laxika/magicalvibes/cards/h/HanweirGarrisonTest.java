package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HanweirGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two 1/1 Human tokens tapped and attacking")
    void attackCreatesTokensTappedAndAttacking() {
        Permanent garrison = new Permanent(new HanweirGarrison());
        garrison.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(garrison);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .toList();
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Does not create tokens when not attacking")
    void noTokensWhenNotAttacking() {
        Permanent garrison = new Permanent(new HanweirGarrison());
        garrison.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(garrison);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count()).isZero();
    }
}
