package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeoninWarleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two 1/1 white Cat tokens with lifelink, tapped and attacking")
    void attackCreatesLifelinkCatTokens() {
        Permanent warleader = new Permanent(new LeoninWarleader());
        warleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(warleader);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> cats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"))
                .toList();
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
    }

    @Test
    @DisplayName("No tokens are created when Leonin Warleader does not attack")
    void noTriggerWithoutAttacking() {
        Permanent warleader = new Permanent(new LeoninWarleader());
        warleader.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(warleader);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }
}
