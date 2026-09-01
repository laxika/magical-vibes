package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Festival.class, BogRats.class})
class FestivalTest extends BaseCardTest {

    @Test
    @DisplayName("During an opponent's upkeep, all creatures can't attack this turn")
    void allCreaturesCantAttackThisTurn() {
        Permanent player1Creature = addCreatureReady(player1);
        Permanent player2Creature = addCreatureReady(player2);
        castFestivalDuringOpponentUpkeep();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).isEmpty();
        assertThat(player1Creature.isCantAttackThisTurn()).isTrue();
        assertThat(player2Creature.isCantAttackThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Also stops creatures that enter later in the turn")
    void creaturesEnteringLaterThisTurnCantAttack() {
        castFestivalDuringOpponentUpkeep();
        addCreatureReady(player2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack restriction clears at the next turn")
    void restrictionClearsAtNextTurn() {
        Permanent bear = addCreatureReady(player1);
        castFestivalDuringOpponentUpkeep();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);
        assertThat(bear.isCantAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast outside an opponent's upkeep")
    void cannotCastOutsideOpponentUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromHand(player1, new Festival(), "{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new BogRats());
    }

    private void castFestivalDuringOpponentUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.castFromHand(player1, new Festival(), "{W}");
        harness.passBothPriorities();
    }
}
