package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZilorthaStrengthIncarnate.class, GoblinPiker.class, GiantSpider.class, YavimayaWurm.class})
class ZilorthaStrengthIncarnateTest extends BaseCardTest {

    @Test
    void ownCreaturesUsePowerToDetermineLethalDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());

        piker.addMarkedDamage(null, 1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(piker);
        assertThat(gqs.getLethalDamageThreshold(gd, piker)).isEqualTo(2);
    }

    @Test
    void opposingCreaturesStillUseToughnessForLethalDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player2, new GoblinPiker());

        piker.addMarkedDamage(null, 1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(piker);
    }

    @Test
    void trampleUsesPowerBasedLethalDamageForBlockerAssignment() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaWurm());
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
