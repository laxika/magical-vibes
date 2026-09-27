package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AutochthonWurm.class, ElvesOfDeepShadow.class, GrayscaledGharial.class})
class AutochthonWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new ElvesOfDeepShadow());
        harness.setHand(player1, List.of(new AutochthonWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof AutochthonWurm)
                .hasSize(1);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new AutochthonWurm());
        Permanent blocker = addCreatureReady(player2, new GrayscaledGharial());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 8));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
