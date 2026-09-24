package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RuhanOfTheFomori.class)
class RuhanOfTheFomoriTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Ruhan must attack its randomly chosen opponent")
    void beginningOfCombatChoosesOpponentAndForcesAttack() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(ruhan.isMustAttackThisCombat()).isTrue();
        assertThat(ruhan.getMustAttackTargetId()).isEqualTo(player2.getId());

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        gs.declareAttackers(gd, player1, List.of(0));
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("The random-opponent attack requirement ends with combat")
    void requirementEndsWithCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(ruhan.isMustAttackThisCombat()).isTrue();

        beginDeclareAttackers(player1);
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    private Permanent addReadyRuhan(Player player) {
        Permanent ruhan = harness.addToBattlefieldAndReturn(player, new RuhanOfTheFomori());
        ruhan.setSummoningSick(false);
        return ruhan;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
