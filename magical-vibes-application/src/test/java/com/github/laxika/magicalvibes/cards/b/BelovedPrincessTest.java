package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelovedPrincess.class, GrizzlyBears.class})
class BelovedPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("Beloved Princess can't be blocked by a creature with power 3")
    void cannotBeBlockedByPowerThree() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = addCreatureWithStats(3, 3);

        beginDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, princess))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Beloved Princess can be blocked by a creature with power 2")
    void canBeBlockedByPowerTwo() {
        Permanent princess = addPrincess();
        princess.setAttacking(true);
        Permanent blocker = addCreatureWithStats(2, 2);

        beginDeclareBlockers();
        declareBlock(blocker, princess);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Lifelink gains life from combat damage to a player")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent princess = addPrincess();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addPrincess() {
        Permanent princess = new Permanent(new BelovedPrincess());
        princess.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(princess);
        return princess;
    }

    private Permanent addCreatureWithStats(int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
