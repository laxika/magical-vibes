package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneTongueBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold makes all able creatures block Stone-Tongue Basilisk")
    void thresholdForcesAllAbleCreaturesToBlock() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent basilisk = addAttackingCreature(player1, new StoneTongueBasilisk());
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(basilisk.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage schedules the damaged creature for destruction at end of combat")
    void combatDamageDestroysCreatureAtEndOfCombat() {
        Permanent basilisk = addAttackingCreature(player1, new StoneTongueBasilisk());
        Permanent dreadmaw = addReadyCreature(player2, new ColossalDreadmaw());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(basilisk);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dreadmaw);
    }

    @Test
    @DisplayName("The trigger does not apply when another creature deals the combat damage")
    void triggerIsSelfScoped() {
        Permanent basilisk = addReadyCreature(player1, new StoneTongueBasilisk());
        Permanent attacker = addAttackingCreature(player1, new GrizzlyBears());
        Permanent dreadmaw = addReadyCreature(player2, new ColossalDreadmaw());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(basilisk);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dreadmaw);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        return super.addCreatureReady(player, card);
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent permanent = addReadyCreature(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
