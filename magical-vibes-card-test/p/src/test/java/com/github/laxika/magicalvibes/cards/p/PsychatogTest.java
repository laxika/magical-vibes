package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Psychatog.class, AvenFisher.class, Firebolt.class})
class PsychatogTest extends BaseCardTest {

    @Test
    void discardingACardBoostsPsychatog() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setHand(player1, List.of(new AvenFisher()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(1);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Aven Fisher");
    }

    @Test
    void exilingTwoGraveyardCardsBoostsPsychatog() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setGraveyard(player1, List.of(new AvenFisher(), new Firebolt()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(1);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void cannotExileTwoCardsWithoutTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new Psychatog());
        harness.setGraveyard(player1, List.of(new AvenFisher()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsWearOffAtEndOfTurn() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setHand(player1, List.of(new AvenFisher()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();
    }

    @Test
    void cannotDiscardWithoutCardInHand() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();
    }

    @Test
    void exilesExactlyTwoChosenCardsFromLargerGraveyard() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        AvenFisher first = new AvenFisher();
        Firebolt second = new Firebolt();
        AvenFisher remaining = new AvenFisher();
        harness.setGraveyard(player1, List.of(first, second, remaining));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(1);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void cannotExileCardsFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Psychatog());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new AvenFisher(), new Firebolt()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void discardedCardsCanPayExileCostBeforeDiscardAbilitiesResolve() {
        Permanent psychatog = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        AvenFisher first = new AvenFisher();
        Firebolt second = new Firebolt();
        harness.setHand(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isEqualTo(3);
        assertThat(psychatog.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(psychatog.getPowerModifier()).isZero();
        assertThat(psychatog.getToughnessModifier()).isZero();
    }

    @Test
    void boostAppliesOnlyToThePsychatogWhoseAbilityWasActivated() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Psychatog());
        harness.setGraveyard(player1, List.of(new AvenFisher(), new Firebolt()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }
}
