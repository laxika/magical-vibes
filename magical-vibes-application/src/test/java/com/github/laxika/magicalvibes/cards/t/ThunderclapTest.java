package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thunderclap.class, JhovallQueen.class, JhovallRider.class, Mountain.class, Island.class})
class ThunderclapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target creature when cast for mana")
    void dealsDamageWhenCastForMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Thunderclap()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Jhovall Rider");
        harness.assertInGraveyard(player2, "Jhovall Rider");
    }

    @Test
    @DisplayName("Deals exactly 3 damage to a creature that survives")
    void dealsExactlyThreeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JhovallQueen());
        harness.setHand(player1, List.of(new Thunderclap()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Jhovall Queen");
    }

    @Test
    @DisplayName("Alternate cost: sacrifices a Mountain instead of mana")
    void castsBySacrificingMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Thunderclap()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(mountain.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Jhovall Rider");
        harness.assertInGraveyard(player2, "Jhovall Rider");
        harness.assertInGraveyard(player1, "Thunderclap");
    }

    @Test
    @DisplayName("Alternate cost requires a Mountain")
    void alternateCostRejectsNonMountain() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Thunderclap()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Thunderclap");
    }

    @Test
    @DisplayName("Alternate cost cannot sacrifice an opponent's Mountain")
    void alternateCostRejectsOpponentsMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JhovallRider());
        harness.setHand(player1, List.of(new Thunderclap()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInHand(player1, "Thunderclap");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Thunderclap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
