package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnollspineInvocationTest extends BaseCardTest {

    // ===== Discard-cost choice (mana value must equal X) =====

    @Test
    @DisplayName("Activating with X=2 only offers cards with mana value 2 for the discard cost")
    void discardChoiceRestrictedToManaValueX() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        // GrizzlyBears has mana value 2, Ornithopter has mana value 0
        harness.setHand(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate for X=2 with no mana-value-2 card in hand")
    void cannotActivateWithoutManaValueXCard() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new Ornithopter())); // mana value 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a");
    }

    // ===== Damage to a player =====

    @Test
    @DisplayName("Deals X damage to target player, discarding the chosen card")
    void dealsXDamageToPlayer() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getXValue()).isEqualTo(2);
        // The mana-value-2 card was discarded to pay the cost
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Damage to a creature =====

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreature() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 2, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears (2/2) destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== X=0 edge case =====

    @Test
    @DisplayName("X=0 discards a mana-value-0 card and deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new Ornithopter())); // mana value 0
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }
}
