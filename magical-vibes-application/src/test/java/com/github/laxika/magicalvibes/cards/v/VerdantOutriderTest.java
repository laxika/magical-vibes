package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantOutrider.class, GrizzlyBears.class, HillGiant.class})
class VerdantOutriderTest extends BaseCardTest {

    @Test
    void activatedAbilityPreventsPowerTwoOrLessCreaturesFromBlockingThisTurn() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareBlockerDeclaration(outrider);

        assertThatThrownBy(() -> declareBlock(bears, outrider))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    void activatedAbilityAllowsPowerThreeOrGreaterCreaturesToBlockThisTurn() {
        Permanent outrider = addCreatureReady(player1, new VerdantOutrider());
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        prepareBlockerDeclaration(outrider);
        declareBlock(hillGiant, outrider);

        assertThat(hillGiant.isBlocking()).isTrue();
    }

    private void prepareBlockerDeclaration(Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
