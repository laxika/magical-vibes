package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultSquad.class, CatapultMaster.class})
class CatapultSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two Soldiers deals 2 damage to an attacking creature")
    void tapsTwoSoldiersAndDamagesAttacker() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        Permanent soldier = addCreatureReady(player1, new CatapultMaster());
        Permanent attacker = addAttackingCreature(player2);

        harness.activateAbility(player1, battlefieldIndex(squad), null, attacker.getId());
        harness.passBothPriorities();

        assertThat(squad.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void rejectsNonCombatTarget() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        addCreatureReady(player1, new CatapultMaster());
        Permanent target = addCreatureReady(player2, new CatapultMaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(squad), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot activate without two untapped Soldiers")
    void requiresTwoUntappedSoldiers() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        Permanent attacker = addAttackingCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(squad), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void damagesBlockingCreature() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        Permanent soldier = addCreatureReady(player1, new CatapultMaster());
        Permanent attacker = addCreatureReady(player1, new CatapultMaster());
        Permanent blocker = addCreatureReady(player2, new CatapultMaster());

        declareAttackers(List.of(2));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));

        harness.activateAbility(player1, battlefieldIndex(squad), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(squad.isTapped()).isTrue();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Soldiers controlled by an opponent do not pay the cost")
    void requiresSoldiersYouControl() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        Permanent opponentSoldier = addCreatureReady(player2, new CatapultMaster());
        Permanent attacker = addAttackingCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(squad), null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(squad.isTapped()).isFalse();
        assertThat(opponentSoldier.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isFalse();
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent attacker = addCreatureReady(player, new CatapultMaster());
        attacker.setAttacking(true);
        return attacker;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
