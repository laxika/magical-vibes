package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.Cactarantula;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnscrupulousContractor.class, Cactarantula.class, Plains.class})
class UnscrupulousContractorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes the target player draw two cards and lose 2 life")
    void sacrificeCreatureTriggersTargetPlayerDrawAndLifeLoss() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        harness.setLibrary(player2, List.of(new Cactarantula(), new Cactarantula()));
        castContractor();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sacrifice.getCard().getId()));
    }

    @Test
    @DisplayName("Declining the sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        castContractor();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ETB sacrifice can only choose a creature")
    void sacrificeChoiceOnlyOffersCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Cactarantula());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        castContractor();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        Permanent contractor = findPermanent(player1, "Unscrupulous Contractor");
        assertThat(sacrificeChoice.validIds()).containsExactlyInAnyOrder(creature.getId(), contractor.getId());
        assertThat(sacrificeChoice.validIds()).doesNotContain(land.getId());
    }

    @Test
    void canSacrificeItselfAndTargetItsController() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        castContractor();
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent contractor = findPermanent(player1, "Unscrupulous Contractor");

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, contractor.getId());
        harness.assertInGraveyard(player1, "Unscrupulous Contractor");
        harness.assertNotOnBattlefield(player1, "Unscrupulous Contractor");
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificeChoiceExcludesOpponentsCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Cactarantula());
        castContractor();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(findPermanent(player1, "Unscrupulous Contractor").getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());
    }

    @Test
    void plotPaysThreeManaAndAllowsFreeCastingOnlyOnALaterTurnAtSorcerySpeed() {
        UnscrupulousContractor contractor = new UnscrupulousContractor();
        harness.setHand(player1, List.of(contractor));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(contractor);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, contractor.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, contractor.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player2, contractor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, contractor.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, contractor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Unscrupulous Contractor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(contractor);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void plottingRequiresFullCostAndSorceryTiming() {
        UnscrupulousContractor contractor = new UnscrupulousContractor();
        harness.setHand(player1, List.of(contractor));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Unscrupulous Contractor");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Unscrupulous Contractor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    private void castContractor() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new UnscrupulousContractor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
    }
}
