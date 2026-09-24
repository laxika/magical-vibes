package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuhanOfTheFomori.class})
class RuhanOfTheFomoriTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's combat, Ruhan chooses the opponent and must attack this combat")
    void choosesOpponentAndMustAttackThisCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(ruhan.isMustAttackThisCombat()).isTrue();
        assertThat(ruhan.getMustAttackTargetId()).isEqualTo(player2.getId());

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Ruhan's requirement is combat-scoped")
    void requirementIsCombatScoped() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(ruhan.isMustAttackThisCombat()).isTrue();

        ruhan.clearCombatState();

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Ruhan does not trigger on an opponent's combat")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent ruhan = addReadyRuhan(player1);

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(ruhan.isMustAttackThisCombat()).isFalse();
    }

    private Permanent addReadyRuhan(Player player) {
        Permanent ruhan = new Permanent(new RuhanOfTheFomori());
        ruhan.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ruhan);
        return ruhan;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
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
