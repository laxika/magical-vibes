package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pyromatics.class, GrizzlyBears.class, Plains.class})
class PyromaticsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPyromatics(target.getId(), List.of());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of("{1}{R}", "{1}{R}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPyromatics(UUID targetId, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 2 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
