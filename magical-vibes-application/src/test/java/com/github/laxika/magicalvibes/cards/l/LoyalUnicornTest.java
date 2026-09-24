package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalUnicorn.class, GrizzlyBears.class, HillGiant.class})
class LoyalUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant grants other creatures vigilance until end of turn")
    void lieutenantGrantsVigilanceUntilEndOfTurn() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant does not trigger without controlling a commander")
    void lieutenantDoesNotTriggerWithoutCommander() {
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant prevents combat damage to creatures you control")
    void lieutenantPreventsCombatDamageToYourCreatures() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        advanceToCombatAndResolve(player1);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    private void addCommander(Player player) {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
