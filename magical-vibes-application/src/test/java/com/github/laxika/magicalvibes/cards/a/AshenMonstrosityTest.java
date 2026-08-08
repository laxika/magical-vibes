package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshenMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ashen Monstrosity puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AshenMonstrosity()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ashen Monstrosity");
    }

    @Test
    @DisplayName("Ashen Monstrosity deals 7 combat damage when unblocked")
    void dealsSevenDamageUnblocked() {
        harness.setLife(player2, 20);

        Permanent monstrosity = new Permanent(new AshenMonstrosity());
        monstrosity.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(monstrosity);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Declaring no attackers when Ashen Monstrosity can attack throws exception")
    void mustAttackWhenAble() {
        Permanent monstrosity = new Permanent(new AshenMonstrosity());
        monstrosity.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(monstrosity);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Ashen Monstrosity from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        Permanent monstrosity = new Permanent(new AshenMonstrosity());
        monstrosity.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(monstrosity);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haste lets a summoning-sick Ashen Monstrosity attack, and it must do so")
    void hasteMakesItAttackTheTurnItEnters() {
        harness.setLife(player2, 20);

        Permanent monstrosity = new Permanent(new AshenMonstrosity());
        gd.playerBattlefields.get(player1.getId()).add(monstrosity);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
