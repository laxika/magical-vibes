package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkbladeAgent.class, DimirInformant.class})
class DarkbladeAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Has no surveil reward before surveiling")
    void rewardIsInactiveBeforeSurveiling() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch and draws after dealing combat damage after surveiling")
    void gainsAbilitiesAfterSurveiling() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        Card first = new DimirInformant();
        Card second = new DimirInformant();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();

        harness.setLibrary(player1, List.of(new DimirInformant()));
        harness.setHand(player1, List.of());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The surveil reward resets at the start of the next turn")
    void rewardResetsAtStartOfNextTurn() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        harness.setLibrary(player1, List.of(new DimirInformant()));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void combatDamageWithoutSurveilingDoesNotDraw() {
        addCreatureReady(player1, new DarkbladeAgent());
        harness.setLife(player2, 20);
        Card topCard = new DimirInformant();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentsSurveilDoesNotGrantAbilities() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        harness.setLibrary(player2, List.of(new DimirInformant()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DimirInformant(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void surveilingBeforeAgentEntersStillGrantsAbilities() {
        harness.setLibrary(player1, List.of(new DimirInformant()));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void surveilingAnEmptyLibraryStillGrantsDeathtouch() {
        Permanent agent = addCreatureReady(player1, new DarkbladeAgent());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, agent, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
