package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubblebeltRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Rubblebelt Recluse must attack each combat if able")
    void mustAttackWhenAble() {
        Permanent recluse = new Permanent(new RubblebeltRecluse());
        recluse.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(recluse);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
