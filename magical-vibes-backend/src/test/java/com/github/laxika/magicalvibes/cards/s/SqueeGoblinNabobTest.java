package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqueeGoblinNabobTest extends BaseCardTest {


    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Squee, Goblin Nabob has correct card properties")
    void hasCorrectProperties() {
        SqueeGoblinNabob card = new SqueeGoblinNabob();

        assertThat(card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(ReturnSelfFromGraveyardToHandEffect.class);
    }

    @Test
    @DisplayName("Triggers during its owner's upkeep while in graveyard")
    void triggersDuringOwnersUpkeepFromGraveyard() {
        harness.setGraveyard(player1, List.of(new SqueeGoblinNabob()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Squee, Goblin Nabob");
    }

    @Test
    @DisplayName("Does not trigger during opponent upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.setGraveyard(player1, List.of(new SqueeGoblinNabob()));

        advanceToUpkeep(player2);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting the trigger returns Squee from graveyard to hand")
    void acceptsTriggerAndReturnsToHand() {
        SqueeGoblinNabob squee = new SqueeGoblinNabob();
        harness.setGraveyard(player1, List.of(squee));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

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
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(squee.getId()));
    }
}


