package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzaAssemblesTheTitans.class, Forest.class, JaceBeleren.class, GarrukWildspeaker.class})
class UrzaAssemblesTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I scries four and puts a revealed planeswalker into hand")
    void chapterIRevealsPlaneswalkerIntoHand() {
        Card planeswalker = new JaceBeleren();
        harness.setLibrary(player1, List.of(planeswalker, new Forest(), new Forest(), new Forest(), new Forest()));
        addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(4);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(1, 2, 3)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(planeswalker);
    }

    @Test
    @DisplayName("Chapter II puts only a planeswalker with mana value six or less from hand onto the battlefield")
    void chapterIIPutsEligiblePlaneswalkerFromHand() {
        Card eligiblePlaneswalker = new JaceBeleren();
        eligiblePlaneswalker.setLoyalty(3);
        Card ineligibleCard = new Forest();
        harness.setHand(player1, List.of(eligiblePlaneswalker, ineligibleCard));
        addSagaWithLore(1);

        triggerChapter();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> "Jace Beleren".equals(permanent.getCard().getName()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ineligibleCard);
    }

    @Test
    @DisplayName("Chapter III grants two loyalty activations for the turn")
    void chapterIIIGrantsExtraLoyaltyActivation() {
        addSagaWithLore(2);
        Permanent garruk = addReadyGarruk(player1);

        triggerChapter();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }

    @Test
    void readAheadCanStartAtChapterThree() {
        addReadyGarruk(player1);
        harness.castFromHand(player1, new UrzaAssemblesTheTitans(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urza Assembles the Titans");
        harness.assertNotOnBattlefield(player1, "Urza Assembles the Titans");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    @Test
    void chapterIIIAffectsPlaneswalkersEnteringLater() {
        addSagaWithLore(2);
        triggerChapter();
        Permanent garruk = harness.enterBattlefieldAndReturn(player1, new GarrukWildspeaker());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void multipleChapterIIIResolutionsStillAllowOnlyTwoActivations() {
        addSagaWithLore(2);
        addSagaWithLore(2);
        Permanent garruk = addReadyGarruk(player1);
        triggerChapter();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }

    @Test
    void chapterICanDeclineToReveal() {
        Card planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(planeswalker));
        addSagaWithLore(0);
        triggerChapter();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(planeswalker);
    }

    @Test
    void chapterILeavesRevealedNonPlaneswalkerOnTop() {
        Card land = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(land));
        addSagaWithLore(0);
        triggerChapter();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void chapterIICanDeclineToPutPlaneswalkerOntoBattlefield() {
        Card planeswalker = new JaceBeleren();
        harness.setHand(player1, List.of(planeswalker));
        addSagaWithLore(1);
        triggerChapter();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(planeswalker);
        harness.assertNotOnBattlefield(player1, "Jace Beleren");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new UrzaAssemblesTheTitans());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addReadyGarruk(Player player) {
        Permanent garruk = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        garruk.setSummoningSick(false);
        return garruk;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
