package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SpireGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmissaryOfDespair.class, DarksteelIngot.class, DrossGolem.class, DroolingOgre.class,
        SpireGolem.class})
class EmissaryOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Damaged player loses life for each artifact they control")
    void losesLifePerArtifactControlledByDamagedPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player1, new DarksteelIngot());
        addCreatureReady(player2, new DarksteelIngot());
        addCreatureReady(player2, new DrossGolem());
        addCreatureReady(player2, new DroolingOgre());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Causes no extra life loss when the damaged player controls no artifacts")
    void noExtraLifeLossWithoutArtifacts() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player2, new DroolingOgre());

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EmissaryOfDespair());
        addCreatureReady(player2, new DarksteelIngot());
        Permanent blocker = addCreatureReady(player2, new SpireGolem());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveCombatAndTrigger() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
    }
}
