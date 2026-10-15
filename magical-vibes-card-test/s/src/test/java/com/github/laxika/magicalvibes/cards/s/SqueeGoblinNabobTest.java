package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SqueeGoblinNabob.class, ShockTroops.class})
class SqueeGoblinNabobTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers during its owner's upkeep while in graveyard")
    void triggersDuringOwnersUpkeepFromGraveyard() {
        harness.setGraveyard(player1, List.of(new SqueeGoblinNabob()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Squee, Goblin Nabob");
    }

    @Test
    @DisplayName("Does not trigger during opponent upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new SqueeGoblinNabob()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the trigger returns Squee from graveyard to hand")
    void acceptsTriggerAndReturnsToHand() {
        SqueeGoblinNabob squee = new SqueeGoblinNabob();
        harness.setGraveyard(player1, List.of(squee));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(squee.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(squee.getId()));
    }

    @Test
    @DisplayName("Declining the trigger keeps Squee in graveyard")
    void declineKeepsSqueeInGraveyard() {
        SqueeGoblinNabob squee = new SqueeGoblinNabob();
        harness.setGraveyard(player1, List.of(squee));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(squee.getId()));
    }

    @Test
    @DisplayName("Returns Squee but not another card in the graveyard")
    void returnsOnlySqueeFromGraveyard() {
        SqueeGoblinNabob squee = new SqueeGoblinNabob();
        ShockTroops otherCard = new ShockTroops();
        harness.setGraveyard(player1, List.of(squee, otherCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(squee.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("Does not trigger from the battlefield, hand, or exile")
    void doesNotTriggerOutsideGraveyard() {
        harness.addToBattlefield(player1, new SqueeGoblinNabob());
        harness.setHand(player1, List.of(new SqueeGoblinNabob()));
        harness.setExile(player1, List.of(new SqueeGoblinNabob()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each Squee in the graveyard has an independent optional return")
    void multipleCopiesReturnIndependently() {
        SqueeGoblinNabob first = new SqueeGoblinNabob();
        SqueeGoblinNabob second = new SqueeGoblinNabob();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        var returned = gd.playerHands.get(player1.getId()).getFirst();
        var remaining = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(List.<Card>of(first, second)).contains(returned, remaining);
        assertThat(returned).isNotSameAs(remaining);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not return Squee after it leaves and reenters the graveyard")
    void doesNotReturnNewGraveyardIncarnation() {
        SqueeGoblinNabob squee = new SqueeGoblinNabob();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(squee));
        gd.markGraveyardEntry(squee);

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(squee));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(squee));
        gd.markGraveyardEntry(squee);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(squee);
    }
}
