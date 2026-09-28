package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSlaanesh.class, GrizzlyBears.class})
class SeekerOfSlaaneshTest extends BaseCardTest {

    @Test
    void eachOpponentMustAttackWithAtLeastOneCreature() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        beginAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must attack with at least one creature");
    }

    @Test
    void oneOpponentCreatureCanAttack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        beginAttackers(player2);
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    void controllerIsNotRequiredToAttack() {
        harness.addToBattlefield(player1, new SeekerOfSlaanesh());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        beginAttackers(player1);
        gs.declareAttackers(gd, player1, List.of());
    }

    private void beginAttackers(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
