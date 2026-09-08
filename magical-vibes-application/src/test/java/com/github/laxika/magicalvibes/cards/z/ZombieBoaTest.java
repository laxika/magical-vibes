package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({ZombieBoa.class, CrawWurm.class})
class ZombieBoaTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prompts for a color and destroys a blocker of that color")
    void destroysBlockerOfChosenColor() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new CrawWurm());

        activateAndChoose("GREEN");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("A blocker of another color is not destroyed")
    void keepsBlockerOfAnotherColor() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new CrawWurm());

        activateAndChoose("RED");
        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent boa = addReadyZombieBoa();
        Permanent blocker = addCreatureReady(player2, new CrawWurm());

        activateAndChoose("GREEN");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        boa.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    private Permanent addReadyZombieBoa() {
        Permanent boa = harness.addToBattlefieldAndReturn(player1, new ZombieBoa());
        boa.setSummoningSick(false);
        return boa;
    }

    private void activateAndChoose(String color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, color);
    }
}
