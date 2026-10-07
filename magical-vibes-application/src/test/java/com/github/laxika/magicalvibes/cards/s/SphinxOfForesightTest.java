package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfForesight.class})
class SphinxOfForesightTest extends BaseCardTest {

    @Test
    void openingHandRevealScryThree() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer1(), List.of(new SphinxOfForesight()));
        h.skipMulligan();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        h.handleMayAbilityChosen(h.getPlayer1(), true);
        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNotNull()
                .extracting(PendingInteraction.Scry::cards)
                .asList()
                .hasSize(3);
    }

    @Test
    void decliningOpeningHandRevealDoesNotScry() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer1(), List.of(new SphinxOfForesight()));
        h.skipMulligan();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        h.handleMayAbilityChosen(h.getPlayer1(), false);
        h.passUntil(h.getPlayer1(), TurnStep.PRECOMBAT_MAIN);

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void battlefieldSphinxScriesOneAtUpkeep() {
        GameTestHarness h = newHarness();
        h.addToBattlefield(h.getPlayer1(), new SphinxOfForesight());
        h.skipMulligan();

        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNotNull()
                .extracting(PendingInteraction.Scry::cards)
                .asList()
                .hasSize(1);
    }

    @Test
    void nonStartingPlayerScriesOnTheirOwnFirstUpkeep() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer2(), List.of(new SphinxOfForesight()));
        h.skipMulligan();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        h.handleMayAbilityChosen(h.getPlayer2(), true);
        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        assertThat(h.getGameData().stack).isEmpty();
        assertThat(h.getGameData().interaction.isAwaitingInput()).isFalse();

        h.passUntil(h.getPlayer2(), TurnStep.UPKEEP);
        h.passBothPriorities();

        PendingInteraction.Scry scry = h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.decidingPlayerId()).isEqualTo(h.getPlayer2().getId());
        assertThat(scry.cards()).hasSize(3);
    }

    @Test
    void twoOpeningHandSphinxesScryThreeSeparately() {
        GameTestHarness h = newHarness();
        h.setHand(h.getPlayer1(), List.of(new SphinxOfForesight(), new SphinxOfForesight()));
        h.skipMulligan();

        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        h.handleMayAbilityChosen(h.getPlayer1(), true);
        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        h.handleMayAbilityChosen(h.getPlayer1(), true);
        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();

        PendingInteraction.Scry first = h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(first).isNotNull();
        assertThat(first.cards()).hasSize(3);
        h.getGameService().handleInteractionAnswer(h.getGameData(), h.getPlayer1(),
                new InteractionAnswer.ScryOrder(List.of(2, 1, 0), List.of()));
        h.passBothPriorities();

        PendingInteraction.Scry second = h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(second).isNotNull();
        assertThat(second.cards()).containsExactly(first.cards().get(2), first.cards().get(1), first.cards().get(0));
    }

    @Test
    void battlefieldSphinxTriggersAgainOnOwnUpkeepButNotOpponents() {
        GameTestHarness h = newHarness();
        h.addToBattlefield(h.getPlayer1(), new SphinxOfForesight());
        h.skipMulligan();
        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();
        assertThat(h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        h.getGameService().handleInteractionAnswer(h.getGameData(), h.getPlayer1(),
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        h.passUntilWithNoAttackers(h.getPlayer2(), TurnStep.UPKEEP);
        assertThat(h.getGameData().stack).isEmpty();
        assertThat(h.getGameData().interaction.isAwaitingInput()).isFalse();
        h.passUntilWithNoAttackers(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();

        PendingInteraction.Scry scry = h.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        assertThat(scry.decidingPlayerId()).isEqualTo(h.getPlayer1().getId());
    }

    @Test
    void battlefieldSphinxWithEmptyLibraryFinishesWithoutScryPrompt() {
        GameTestHarness h = newHarness();
        h.setLibrary(h.getPlayer1(), List.of());
        h.addToBattlefield(h.getPlayer1(), new SphinxOfForesight());
        h.skipMulligan();
        h.passUntil(h.getPlayer1(), TurnStep.UPKEEP);
        h.passBothPriorities();

        assertThat(h.getGameData().interaction.isAwaitingInput()).isFalse();
        assertThat(h.getGameData().stack).isEmpty();
        assertThat(h.getGameData().currentStep).isEqualTo(TurnStep.UPKEEP);
    }

    private GameTestHarness newHarness() {
        GameTestHarness h = new GameTestHarness();
        h.getGameData().alwaysOfferPriorityWindows = true;
        h.setLibrary(h.getPlayer1(), List.of(new SphinxOfForesight(), new SphinxOfForesight(), new SphinxOfForesight()));
        h.setLibrary(h.getPlayer2(), List.of(new SphinxOfForesight(), new SphinxOfForesight(), new SphinxOfForesight()));
        return h;
    }
}
