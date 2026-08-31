package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.i.IcatianJavelineers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultSquad.class, IcatianJavelineers.class, AirElemental.class})
class CatapultSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two Soldiers deals 2 damage to an attacking creature")
    void tapsTwoSoldiersAndDamagesAttacker() {
        Permanent squad = addCreatureReady(player1, new CatapultSquad());
        Permanent soldier = addCreatureReady(player1, new IcatianJavelineers());
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
        addCreatureReady(player1, new IcatianJavelineers());
        Permanent target = addCreatureReady(player2, new AirElemental());

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

    private Permanent addAttackingCreature(Player player) {
        Permanent attacker = addCreatureReady(player, new AirElemental());
        attacker.setAttacking(true);
        return attacker;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
