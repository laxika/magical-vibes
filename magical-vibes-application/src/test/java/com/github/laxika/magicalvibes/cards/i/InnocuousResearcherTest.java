package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({InnocuousResearcher.class, Forest.class, GrizzlyBears.class, Shock.class})
class InnocuousResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking investigates for each revealed nonland and draws the revealed cards")
    void parleyInvestigatesForNonlandsAndDraws() {
        Card player1Top = new GrizzlyBears();
        Card player2Top = new GrizzlyBears();
        Card player1Next = new Forest();
        Card player2Next = new Forest();
        harness.setLibrary(player1, List.of(player1Top, player1Next));
        harness.setLibrary(player2, List.of(player2Top, player2Next));
        addReadyResearcher();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Next);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Next);
    }

    @Test
    @DisplayName("Revealed lands do not cause investigation")
    void parleyDoesNotInvestigateForLands() {
        Card player1Top = new Forest();
        Card player2Top = new Forest();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));
        addReadyResearcher();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Top);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Top);
    }

    @Test
    @DisplayName("Accepting the end-step ability untaps lands and prevents casting until your next turn")
    void endStepAbilityUntapsLandsAndPreventsCasting() {
        addReadyResearcher();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        triggerEndStepAbility();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Declining the end-step ability leaves lands tapped and allows casting")
    void decliningEndStepAbilityDoesNotApplyRestriction() {
        addReadyResearcher();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        triggerEndStepAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forest.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("A mixed parley reveal investigates once and both players draw")
    void mixedRevealInvestigatesOnce() {
        Card nonland = new InnocuousResearcher();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(nonland));
        harness.setLibrary(player2, List.of(land));
        addReadyResearcher();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("The end-step ability untaps only its controller's lands")
    void endStepUntapsOnlyControlledLands() {
        Permanent researcher = addReadyResearcher();
        researcher.tap();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownLand.tap();
        opposingLand.tap();

        triggerEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isTrue();
        assertThat(researcher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting with no lands still prevents casting after the Researcher leaves")
    void acceptingWithNoLandsStillPreventsCastingAfterSourceLeaves() {
        Permanent researcher = addReadyResearcher();
        triggerEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(researcher);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The casting restriction lasts through the opponent's turn and ends next turn")
    void castingRestrictionExpiresAtControllersNextTurn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent researcher = addReadyResearcher();
        triggerEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(researcher);
        harness.setHand(player1, List.of(new Shock()));

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private Permanent addReadyResearcher() {
        return addCreatureReady(player1, new InnocuousResearcher());
    }

    private void triggerEndStepAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
