package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FireLitThicket;
import com.github.laxika.magicalvibes.cards.s.SafeholdSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnollspineInvocation.class, SafeholdSentry.class, FireLitThicket.class})
class KnollspineInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with X=2 only offers cards with mana value 2 for the discard cost")
    void discardChoiceRestrictedToManaValueX() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        // Safehold Sentry has mana value 2, Fire-Lit Thicket has mana value 0
        harness.setHand(player1, List.of(new SafeholdSentry(), new FireLitThicket()));
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
        harness.setHand(player1, List.of(new FireLitThicket())); // mana value 0
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a");
    }

    @Test
    @DisplayName("Deals X damage to target player, discarding the chosen card")
    void dealsXDamageToPlayer() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new SafeholdSentry()));
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
        harness.assertInGraveyard(player1, "Safehold Sentry");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals X damage to target creature, destroying it")
    void dealsXDamageToCreature() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.addToBattlefield(player2, new SafeholdSentry());
        harness.setHand(player1, List.of(new SafeholdSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Safehold Sentry");
        harness.activateAbility(player1, 0, 2, targetId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        // Safehold Sentry (2/2) destroyed by 2 damage
        harness.assertNotOnBattlefield(player2, "Safehold Sentry");
        harness.assertInGraveyard(player2, "Safehold Sentry");
    }

    @Test
    @DisplayName("X=0 discards a mana-value-0 card and deals no damage")
    void xZeroDealsNoDamage() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new FireLitThicket())); // mana value 0
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Fire-Lit Thicket");
    }

    @Test
    @DisplayName("Can activate repeatedly without tapping the enchantment")
    void canActivateRepeatedly() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new SafeholdSentry(), new SafeholdSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Knollspine Invocation");
    }

    @Test
    @DisplayName("Any target includes the ability controller")
    void canDamageController() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new SafeholdSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Safehold Sentry");
    }

    @Test
    @DisplayName("A matching card does not allow activation without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new SafeholdSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Safehold Sentry");
        harness.assertNotInGraveyard(player1, "Safehold Sentry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature land is not a legal damage target")
    void cannotTargetNoncreatureLand() {
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.addToBattlefield(player2, new FireLitThicket());
        harness.setHand(player1, List.of(new SafeholdSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Fire-Lit Thicket");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Safehold Sentry");
        assertThat(gd.stack).isEmpty();
    }
}
