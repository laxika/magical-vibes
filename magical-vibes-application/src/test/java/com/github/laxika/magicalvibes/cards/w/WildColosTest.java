package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

class WildColosTest extends BaseCardTest {

    @Test
    @DisplayName("Wild Colos can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        Permanent colos = new Permanent(new WildColos());
        colos.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(colos);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .doesNotThrowAnyException();
    }
}
