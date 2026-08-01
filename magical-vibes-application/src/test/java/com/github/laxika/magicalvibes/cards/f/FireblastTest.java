package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireblastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player when cast for mana")
    void deals4DamageForManaCost() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Fireblast");
    }

    @Test
    @DisplayName("Deals 4 damage to a creature, destroying a 2/2")
    void deals4DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Alternate cost: sacrifice two Mountains instead of mana")
    void castBySacrificingTwoMountains() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Fireblast()));
        harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(mountain1, mountain2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Fireblast");
    }

    @Test
    @DisplayName("Alternate cost fails with fewer than two Mountains")
    void alternateCostFailsWithOneMountain() {
        harness.addToBattlefield(player1, new Mountain());
        UUID mountain = harness.getPermanentId(player1, "Mountain");

        harness.setHand(player1, List.of(new Fireblast()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(mountain)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost fails when sacrificing a non-Mountain")
    void alternateCostFailsWithNonMountain() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        UUID mountain = harness.getPermanentId(player1, "Mountain");
        UUID island = harness.getPermanentId(player1, "Island");

        harness.setHand(player1, List.of(new Fireblast()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateCost(player1, 0, player2.getId(), List.of(mountain, island)))
                .isInstanceOf(IllegalStateException.class);
    }
}
