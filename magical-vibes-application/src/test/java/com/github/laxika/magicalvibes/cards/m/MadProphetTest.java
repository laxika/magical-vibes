package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadProphet.class, Mountain.class, Forest.class})
class MadProphetTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability starts discard-cost choice for any card")
    void activationStartsDiscardChoice() {
        addReadyProphet(player1);
        harness.setHand(player1, List.of(new MadProphet(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Choosing a card pays the discard cost, taps the creature and stacks the ability")
    void choosingCardPaysCostAndStacksAbility() {
        Permanent prophet = addReadyProphet(player1);
        harness.setHand(player1, List.of(new MadProphet(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Mad Prophet");
        harness.assertInGraveyard(player1, "Mad Prophet");
        assertThat(prophet.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        addReadyProphet(player1);
        harness.setHand(player1, List.of(new MadProphet()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without cards in hand")
    void cannotActivateWithoutCardsInHand() {
        addReadyProphet(player1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent prophet = addReadyProphet(player1);
        prophet.tap();
        harness.setHand(player1, List.of(new MadProphet()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can activate the turn it enters the battlefield (Haste)")
    void canActivateWithSummoningSicknessDueToHaste() {
        harness.addToBattlefield(player1, new MadProphet());
        harness.setHand(player1, List.of(new MadProphet()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("A land can pay the discard cost and no card is drawn before resolution")
    void discardingLandPaysCostBeforeDrawing() {
        addReadyProphet(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability still draws after its source leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent prophet = addReadyProphet(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(prophet);
        gd.playerGraveyards.get(player1.getId()).add(prophet.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyProphet(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MadProphet());
        perm.setSummoningSick(false);
        return perm;
    }
}
