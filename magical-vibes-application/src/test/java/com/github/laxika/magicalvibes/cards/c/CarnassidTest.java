package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Carnassid.class, SpinedWurm.class})
class CarnassidTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} grants Carnassid a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent carnassid = addCreatureReady(player1, new Carnassid());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(carnassid.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves Carnassid from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent carnassid = addCreatureReady(player1, new Carnassid());
        carnassid.setRegenerationShield(1);
        Permanent attacker = addCreatureReady(player2, new SpinedWurm());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(carnassid);
        assertThat(carnassid.isTapped()).isTrue();
        assertThat(carnassid.getRegenerationShield()).isZero();
        assertThat(carnassid.getMarkedDamage()).isZero();
        assertThat(carnassid.isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Carnassid dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent carnassid = addCreatureReady(player1, new Carnassid());
        Permanent attacker = addCreatureReady(player2, new SpinedWurm());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(carnassid);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(carnassid.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new Carnassid());
        Permanent blocker = addCreatureReady(player2, new SpinedWurm());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
