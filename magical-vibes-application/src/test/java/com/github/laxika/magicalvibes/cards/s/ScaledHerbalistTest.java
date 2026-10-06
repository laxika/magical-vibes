package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScaledHerbalist.class, Forest.class})
class ScaledHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen land from hand onto the battlefield")
    void putsLandFromHandOntoBattlefield() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        Card forest = new Forest();
        Card nonland = new ScaledHerbalist();
        harness.setHand(player1, List.of(forest, nonland));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(herbalist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the land choice leaves the hand unchanged")
    void decliningLandChoiceDoesNothing() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        Card forest = new Forest();
        harness.setHand(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(herbalist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only one land enters and it enters untapped")
    void putsExactlyOneLandOntoBattlefieldUntapped() {
        addCreatureReady(player1, new ScaledHerbalist());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setHand(player1, List.of(firstLand, secondLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(secondLand);
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting with no lands in hand finishes without putting a card")
    void noLandsInHandDoesNothing() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        Card nonland = new ScaledHerbalist();
        harness.setHand(player1, List.of(nonland));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(herbalist);
        assertThat(herbalist.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Putting a land does not use or require an available land play")
    void worksAfterLandPlayWithoutUsingAnotherLandPlay() {
        addCreatureReady(player1, new ScaledHerbalist());
        harness.setHand(player1, List.of(new Forest()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent herbalist = harness.addToBattlefieldAndReturn(player1, new ScaledHerbalist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(herbalist.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Herbalist cannot activate again")
    void tappedHerbalistCannotActivate() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        herbalist.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
