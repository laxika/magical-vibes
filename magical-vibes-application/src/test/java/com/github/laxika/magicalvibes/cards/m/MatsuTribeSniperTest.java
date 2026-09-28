package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.o.OyobiWhoSplitTheHeavens;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MatsuTribeSniper.class, OyobiWhoSplitTheHeavens.class, GnarledMass.class})
class MatsuTribeSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability damages a flyer, taps it, and locks its next untap step")
    void activatedAbilityTapsAndLocksDamagedFlyer() {
        Permanent sniper = addCreatureReady(player1, new MatsuTribeSniper());
        Permanent flyer = addCreatureReady(player2, new OyobiWhoSplitTheHeavens());

        harness.activateAbility(player1, 0, null, flyer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sniper.isTapped()).isTrue();
        assertThat(flyer.isTapped()).isTrue();
        assertThat(flyer.getSkipUntapCount()).isEqualTo(1);
        assertThat(flyer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new MatsuTribeSniper());
        Permanent creature = addCreatureReady(player2, new GnarledMass());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Combat damage also taps and locks the damaged creature")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent sniper = addCreatureReady(player1, new MatsuTribeSniper());
        sniper.setAttacking(true);
        addCreatureReady(player2, new GnarledMass());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent blocker = findPermanent(player2, "Gnarled Mass");
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unblocked combat damage to a player does not trigger the creature effect")
    void combatDamageToPlayerDoesNotTapOrLockCreature() {
        Permanent sniper = addCreatureReady(player1, new MatsuTribeSniper());
        sniper.setAttacking(true);
        int lifeBefore = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(sniper.getSkipUntapCount()).isZero();
    }
}
