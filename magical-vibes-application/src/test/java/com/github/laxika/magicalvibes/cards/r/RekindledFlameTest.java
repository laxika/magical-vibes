package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RekindledFlame.class, GrizzlyBears.class, Reclaim.class, MentalNote.class})
class RekindledFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target player")
    void deals4DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RekindledFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature, destroying a 2/2")
    void deals4DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RekindledFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Offers to return from graveyard when an opponent has no cards in hand")
    void triggersWhenOpponentHandEmpty() {
        harness.setGraveyard(player1, List.of(new RekindledFlame()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the trigger's MayEffect from the stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Rekindled Flame");
    }

    @Test
    @DisplayName("Does not trigger when every opponent still has cards in hand")
    void doesNotTriggerWhenOpponentHasCards() {
        harness.setGraveyard(player1, List.of(new RekindledFlame()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting returns Rekindled Flame from graveyard to hand")
    void acceptReturnsToHand() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(flame.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(flame.getId()));
    }

    @Test
    @DisplayName("Declining keeps Rekindled Flame in the graveyard")
    void declineKeepsInGraveyard() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(flame.getId()));
    }

    @Test
    @DisplayName("Does nothing if the opponent gains a card before the trigger resolves")
    void rechecksOpponentHandAtResolution() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new RekindledFlame()));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flame);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(flame);
    }

    @Test
    @DisplayName("Emptying the opponent's hand after upkeep begins does not create a trigger")
    void doesNotTriggerLateWhenOpponentHandBecomesEmpty() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of(new RekindledFlame()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flame);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flame);
    }

    @Test
    @DisplayName("Each graveyard copy returns only itself and may be declined independently")
    void multipleCopiesReturnIndependently() {
        RekindledFlame first = new RekindledFlame();
        RekindledFlame second = new RekindledFlame();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        UUID declinedId = gd.pendingMayAbilities.getFirst().sourceCard().getId();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        UUID acceptedId = gd.pendingMayAbilities.getFirst().sourceCard().getId();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(acceptedId).isNotEqualTo(declinedId);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(acceptedId));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(declinedId));
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(c -> c.getId()).containsExactly(declinedId);
    }

    @Test
    @CardUsed({RekindledFlame.class, Reclaim.class, MentalNote.class})
    @DisplayName("An old upkeep trigger cannot return a card that left and reentered the graveyard")
    void doesNotReturnNewGraveyardObject() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player1, List.of(new Reclaim(), new MentalNote()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new RekindledFlame(), new RekindledFlame(), new RekindledFlame()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, flame.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(flame);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flame);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flame);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(flame);
    }
}
