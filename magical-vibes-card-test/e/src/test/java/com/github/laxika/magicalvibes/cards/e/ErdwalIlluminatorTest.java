package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.cards.f.FaeOffering;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErdwalIlluminator.class, ThrabenInspector.class, MagnifyingGlass.class, FaeOffering.class})
class ErdwalIlluminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Investigating for the first time each turn creates an additional Clue")
    void firstInvestigationCreatesAdditionalClue() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger after the first investigation even if it entered later")
    void doesNotTriggerAfterFirstInvestigation() {
        harness.setHand(player1, List.of(new ThrabenInspector(), new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Only the first investigation gets an additional Clue while Illuminator remains in play")
    void subsequentInvestigationDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new ThrabenInspector(), new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Each Illuminator adds one investigation without recursively triggering")
    void multipleIlluminatorsEachTriggerOnce() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's investigation only triggers that opponent's Illuminator")
    void investigationsAreTrackedSeparatelyForEachController() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.addToBattlefield(player2, new ErdwalIlluminator());
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addToBattlefield(player2, new MagnifyingGlass());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 1, 1, null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).hasSize(2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player2, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The first investigation on the opponent's turn triggers again")
    void firstInvestigationResetsOnEveryTurn() {
        harness.addToBattlefield(player1, new ErdwalIlluminator());
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 2, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(4);
    }

    @Test
    @DisplayName("Creating a Clue without investigating does not trigger Illuminator")
    void faeOfferingClueDoesNotCountAsInvestigating() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.setHand(player1, List.of(new ErdwalIlluminator(), new MagnifyingGlass()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 2, 1, null, null);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }
}
