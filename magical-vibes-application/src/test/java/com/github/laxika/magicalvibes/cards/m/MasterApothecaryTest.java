package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MasterApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Cleric prevents 2 damage to a target creature")
    void preventsDamageToCreature() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        assertThat(cleric.isTapped()).isTrue();
        assertThat(apothecary.isTapped()).isFalse();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping an untapped Cleric prevents 2 damage to a target player")
    void preventsDamageToPlayer() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        harness.activateAbility(player1, sourceIndex, null, player2.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without an untapped Cleric to tap")
    void requiresUntappedCleric() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        apothecary.tap();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
