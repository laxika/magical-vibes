package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AkronLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("A non-artifact creature you control cannot attack while Akron Legionnaire is out")
    void nonArtifactCreatureCannotAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Akron Legionnaire itself can attack")
    void akronCanAttack() {
        Permanent akron = new Permanent(new AkronLegionnaire());
        akron.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(akron);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Akron is 8/4, unblocked
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("An artifact creature you control can still attack")
    void artifactCreatureCanAttack() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent thopter = new Permanent(new Ornithopter());
        thopter.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thopter);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int thopterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(thopter);
        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of(thopterIndex)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Opponent's non-artifact creatures are unaffected (restriction is controller-scoped)")
    void opponentCreatureUnaffected() {
        harness.addToBattlefield(player1, new AkronLegionnaire());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        gs.declareAttackers(gd, player2, List.of(bearsIndex));

        assertThat(bears.isAttacking()).isTrue();
    }
}
