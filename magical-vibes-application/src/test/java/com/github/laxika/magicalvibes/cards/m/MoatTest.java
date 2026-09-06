package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moat.class, GrizzlyBears.class, SuntailHawk.class})
class MoatTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying cannot attack while Moat is on the battlefield")
    void nonFlyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new Moat());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying can attack while Moat is on the battlefield")
    void flyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new Moat());
        addCreatureReady(player2, new SuntailHawk());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creatures can attack again after Moat leaves the battlefield")
    void restrictionEndsWhenMoatLeaves() {
        Permanent moat = harness.addToBattlefieldAndReturn(player1, new Moat());
        addCreatureReady(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(moat);
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }
}
