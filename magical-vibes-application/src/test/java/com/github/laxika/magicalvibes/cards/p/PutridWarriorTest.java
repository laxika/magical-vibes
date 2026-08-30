package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PutridWarrior.class)
class PutridWarriorTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Each player loses 1 life.";
    private static final String GAIN_LIFE = "Each player gains 1 life.";

    @Test
    @DisplayName("Choosing the life-loss mode makes each player lose 1 life")
    void eachPlayerLosesLife() {
        addAttacker();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndChoose(LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Choosing the life-gain mode makes each player gain 1 life")
    void eachPlayerGainsLife() {
        addAttacker();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndChoose(GAIN_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No damage means no trigger")
    void noDamageDoesNotTrigger() {
        PutridWarrior warrior = new PutridWarrior();
        warrior.setPower(0);
        addAttacker(warrior);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private PutridWarrior addAttacker() {
        return addAttacker(new PutridWarrior());
    }

    private PutridWarrior addAttacker(PutridWarrior warrior) {
        Permanent permanent = new Permanent(warrior);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return warrior;
    }

    private void resolveCombatAndChoose(String mode) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
