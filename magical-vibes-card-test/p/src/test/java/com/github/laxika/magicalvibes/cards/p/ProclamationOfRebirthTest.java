package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProclamationOfRebirth.class, LlanowarElves.class, GrizzlyBears.class, HillGiant.class,
        HallowedFountain.class, Ornithopter.class})
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
        assertThat(choice.minCount()).isZero();
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
    @DisplayName("Can choose fewer than three eligible creature cards")
    void canChooseFewerThanThreeEligibleCreatures() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        Card tooExpensive = new HillGiant();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(first, second, tooExpensive));
        harness.setHand(player1, List.of(proclamation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(second.getId(), tooExpensive.getId(), proclamation.getId());
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
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ProclamationOfRebirth()));
        advanceToUpkeep(player1);
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

    @Test
    @DisplayName("Forecast cannot be activated during an opponent's upkeep")
    void forecastRequiresYourUpkeep() {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(proclamation));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
    }

    @Test
    @DisplayName("Forecast requires a legal graveyard target")
    void forecastRequiresLegalGraveyardTarget() {
        Card ineligible = new HillGiant();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(ineligible));
        harness.setHand(player1, List.of(proclamation));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must select graveyard targets");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ineligible);
    }
    @Test
    @DisplayName("Returns a zero-mana artifact creature but excludes a land with mana value zero")
    void returnsZeroManaCreatureButNotLand() {
        Card creature = new Ornithopter();
        Card land = new HallowedFountain();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(proclamation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land, proclamation);
    }

    @Test
    @DisplayName("Can choose zero targets even when eligible creatures exist")
    void canChooseZeroTargets() {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(proclamation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, proclamation);
    }

    @Test
    @DisplayName("Can be cast with an empty graveyard")
    void canCastWithEmptyGraveyard() {
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(proclamation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(proclamation);
    }

    @Test
    @DisplayName("Returns remaining legal targets when another target leaves the graveyard")
    void returnsRemainingLegalTargets() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ProclamationOfRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getId());
    }

    @Test
    @DisplayName("Forecast cannot target a creature in an opponent's graveyard")
    void forecastRejectsOpponentsGraveyard() {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(proclamation));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(proclamation);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Forecast keeps only its source revealed until the upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() throws Exception {
        Card creature = new LlanowarElves();
        ProclamationOfRebirth proclamation = new ProclamationOfRebirth();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(proclamation, new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(creature.getId()));
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.passBothPriorities());
        harness.publishState();

        String message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).extracting(card -> card.id()).containsExactly(proclamation.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.publishState();
        message = harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        assertThat(state.opponentHand()).isEmpty();
    }
}
