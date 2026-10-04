package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FurnaceHostCharger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpharasDispersal.class, FurnaceHostCharger.class, Island.class})
class EpharasDispersalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature and surveils two")
    void returnsCreatureAndSurveilsTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        Card topCard = new FurnaceHostCharger();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Furnace Host Charger");
        assertThat(gameData.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));

        PendingInteraction.Scry surveil = gameData.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gameData.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Costs only {U} when targeting an attacking creature")
    void reducedCostWhenTargetingAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires the full cost when targeting a nonattacking creature")
    void fullCostWhenTargetingNonattackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnOwnCreatureAndKeepBothCardsInEitherOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FurnaceHostCharger());
        Card first = new Island();
        Card second = new FurnaceHostCharger();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        harness.assertNotOnBattlefield(player1, "Furnace Host Charger");
        harness.assertInHand(player1, "Furnace Host Charger");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
    }

    @Test
    void canPutBothSurveilledCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        Card first = new Island();
        Card second = new FurnaceHostCharger();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void surveilsAvailableCardWhenLibraryHasOnlyOneCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        Card onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    @Test
    void returnsCreatureWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Furnace Host Charger");
        harness.assertInHand(player2, "Furnace Host Charger");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ephara's Dispersal");
    }

    @Test
    void doesNotSurveilWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        Card first = new Island();
        Card second = new FurnaceHostCharger();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EpharasDispersal(), new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof EpharasDispersal).hasSize(2);
        harness.assertInHand(player2, "Furnace Host Charger");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    void reductionDoesNotRemoveBlueManaRequirement() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new EpharasDispersal()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Ephara's Dispersal");
    }
}
