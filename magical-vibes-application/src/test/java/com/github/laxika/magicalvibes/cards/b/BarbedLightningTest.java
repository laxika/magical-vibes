package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarbedLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode deals 3 damage to target creature")
    void creatureModeDealsDamageToCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(new int[]{0}, List.of(giant.getId()));

        assertThat(giant.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Player mode deals 3 damage to target player")
    void playerModeDealsDamageToPlayer() {
        cast(new int[]{1}, List.of(player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Entwine pays {2} and resolves both modes")
    void entwinedResolvesBothModes() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addMana(2);

        harness.setHand(player1, List.of(new BarbedLightning()));
        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(giant.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Creature mode rejects a player target")
    void creatureModeRejectsPlayerTarget() {
        harness.setHand(player1, List.of(new BarbedLightning()));
        addMana(0);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId())))
                .hasMessageContaining("cannot target players");
    }

    @Test
    @DisplayName("Entwine is rejected without its additional mana")
    void entwineRequiresAdditionalMana() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BarbedLightning()));
        addMana(0);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(giant.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targets) {
        harness.setHand(player1, List.of(new BarbedLightning()));
        addMana(0);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }

    private void addMana(int additionalColorless) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + additionalColorless);
    }
}
