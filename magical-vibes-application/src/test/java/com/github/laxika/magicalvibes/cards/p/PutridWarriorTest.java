package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.cards.q.QuicksilverDagger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridWarrior.class, QuicksilverDagger.class, GoblinLegionnaire.class})
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

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage also triggers the life-choice ability")
    void noncombatDamageTriggers() {
        Permanent warrior = addCreatureReady(player1, new PutridWarrior());

        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new QuicksilverDagger());
        dagger.setAttachedTo(warrior.getId());

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability still triggers when the warrior dies after dealing combat damage")
    void stillTriggersWhenWarriorDiesInCombat() {
        Permanent attacker = addCreatureReady(player1, new PutridWarrior());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoblinLegionnaire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombatAndChoose(LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Putrid Warrior");
        harness.assertInGraveyard(player2, "Goblin Legionnaire");
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
        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
