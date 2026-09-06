package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProclamationOfRebirth.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class})
class ProclamationOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three eligible creature cards from the graveyard")
    void returnsUpToThreeEligibleCreatures() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        Card third = new LlanowarElves();
        Card tooExpensive = new GrizzlyBears();
        Card evenMoreExpensive = new HillGiant();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(first, second, third, tooExpensive, evenMoreExpensive));
        harness.setHand(player1, List.of(proclamation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId(), evenMoreExpensive.getId(),
                        proclamation.getId());
    }

    @Test
    @DisplayName("Rejects more than three graveyard targets")
    void rejectsMoreThanThreeTargets() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        Card third = new LlanowarElves();
        Card fourth = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new ProclamationOfRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Forecast returns one eligible creature and keeps the card in hand")
    void forecastReturnsCreatureAndKeepsSourceInHand() {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(proclamation));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ProclamationOfRebirth()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(first.getId()));

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast cannot be activated outside its controller's upkeep")
    void forecastRequiresUpkeep() {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(proclamation));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
    }
}
