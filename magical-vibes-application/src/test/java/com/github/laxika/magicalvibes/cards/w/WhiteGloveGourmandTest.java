package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteGloveGourmand.class})
class WhiteGloveGourmandTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Human Soldier tokens when it enters")
    void createsHumanSoldierTokensOnEntry() {
        harness.setHand(player1, List.of(new WhiteGloveGourmand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("Creates a Food at your end step when a Human you controlled died")
    void createsFoodAfterHumanDeath() {
        harness.addToBattlefield(player1, new WhiteGloveGourmand());
        gd.creatureSubtypeDeathCountThisTurn.put(player1.getId(),
                Map.of(CardSubtype.HUMAN, 1));

        advanceToControllerEndStep();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Food without a Human death under your control")
    void doesNotCreateFoodWithoutHumanDeathUnderYourControl() {
        harness.addToBattlefield(player1, new WhiteGloveGourmand());
        gd.creatureSubtypeDeathCountThisTurn.put(player2.getId(),
                Map.of(CardSubtype.HUMAN, 1));

        advanceToControllerEndStep();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("A Human dying in combat enables the surviving Gourmand's end-step ability")
    void actualHumanDeathCreatesFood() {
        harness.addToBattlefield(player1, new WhiteGloveGourmand());
        addCreatureReady(player1, new WhiteGloveGourmand());
        addCreatureReady(player2, new WhiteGloveGourmand());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        assertThat(findPermanents(player1, "White Glove Gourmand")).hasSize(1);
        assertThat(findPermanents(player2, "White Glove Gourmand")).isEmpty();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Human deaths create only one Food per Gourmand")
    void multipleHumanDeathsCreateOneFood() {
        harness.addToBattlefield(player1, new WhiteGloveGourmand());
        gd.creatureSubtypeDeathCountThisTurn.put(player1.getId(),
                Map.of(CardSubtype.HUMAN, 3));

        advanceToControllerEndStep();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Food at the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new WhiteGloveGourmand());
        gd.creatureSubtypeDeathCountThisTurn.put(player1.getId(),
                Map.of(CardSubtype.HUMAN, 1));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }
}
