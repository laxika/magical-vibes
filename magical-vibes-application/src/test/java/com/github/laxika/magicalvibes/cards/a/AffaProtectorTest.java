package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AffaProtector.class)
class AffaProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance: Affa Protector does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent affaProtector = new Permanent(new AffaProtector());
        affaProtector.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(affaProtector);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(affaProtector.isTapped()).isFalse();
    }
}
