package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.s.SpireGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryOfHope.class, CrazedGoblin.class, DarksteelIngot.class, SpireGolem.class})
class EmissaryOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life for each artifact controlled by the damaged player")
    void gainsLifePerArtifactControlledByDamagedPlayer() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfHope()).setAttacking(true);
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.addToBattlefield(player2, new SpireGolem());
        harness.addToBattlefield(player2, new CrazedGoblin());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts no artifacts when the damaged player controls none")
    void gainsNoExtraLifeWithoutArtifacts() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfHope()).setAttacking(true);
        harness.addToBattlefield(player2, new CrazedGoblin());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not count artifacts controlled by the attacking player")
    void countsOnlyArtifactsControlledByDamagedPlayer() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfHope()).setAttacking(true);
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player2, new DarksteelIngot());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfHope());
        Permanent blocker = addCreatureReady(player2, new SpireGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
