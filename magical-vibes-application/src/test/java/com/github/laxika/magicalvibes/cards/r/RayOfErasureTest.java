package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayOfErasure.class, BalduvianBears.class})
class RayOfErasureTest extends BaseCardTest {

    @Test
    @DisplayName("Mills one card from the target player and schedules a draw for the caster")
    void millsAndSchedulesDraw() {
        harness.setLibrary(player2, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new RayOfErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card milled = deck.getFirst();
        int deckBefore = deck.size();
        int gyBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(gyBefore + 1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getLast()).isSameAs(milled);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.setLibrary(player1, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new RayOfErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetSelf() {
        harness.setLibrary(player1, List.of(new BalduvianBears()));
        harness.setHand(player1, List.of(new RayOfErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        int deckBefore = deck.size();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new RayOfErasure()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Balduvian Bears")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
