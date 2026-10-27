package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Endurance.class, Forest.class, GrizzlyBears.class})
class EnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts all cards from the target player's graveyard on the bottom of their library")
    void etbPutsTargetGraveyardOnBottom() {
        Card graveyardLand = new Forest();
        Card graveyardCreature = new GrizzlyBears();
        Card existingTop = new Forest();
        harness.setGraveyard(player2, List.of(graveyardLand, graveyardCreature));
        harness.setLibrary(player2, List.of(existingTop));
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(existingTop);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(graveyardLand, graveyardCreature);
    }

    @Test
    @DisplayName("ETB may resolve without a target")
    void etbMayResolveWithoutTarget() {
        Card graveyardCard = new Forest();
        Card ownGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("Evoke exiles a green card and sacrifices Endurance after its ETB")
    void evokeExilesGreenCardAndSacrificesSelf() {
        Card greenCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new Endurance(), greenCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        harness.castInstantWithAlternateExileFromHand(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(greenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Endurance");
        harness.assertNotOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveHardcastMana();
        harness.setHand(player1, List.of(new Endurance()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("ETB can put the controller's graveyard beneath the existing library")
    void etbCanTargetController() {
        Card graveyardCard = new GrizzlyBears();
        Card existingTop = new Forest();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(new Endurance()));
        giveHardcastMana();

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, graveyardCard);
        harness.assertOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("Targeting an empty graveyard leaves the library unchanged")
    void emptyGraveyardLeavesLibraryUnchanged() {
        Card existingTop = new Forest();
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(existingTop));
        harness.setHand(player1, List.of(new Endurance()));
        giveHardcastMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop);
        harness.assertOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("A Forest cannot pay the green-card evoke cost")
    void evokeCannotExileForest() {
        harness.setHand(player1, List.of(new Endurance(), new Forest()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Endurance cannot exile itself to pay its evoke cost")
    void evokeCannotExileItself() {
        harness.setHand(player1, List.of(new Endurance()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, player2.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Evoke works without an ETB target and with the payment before the spell in hand")
    void evokeWithoutTargetExilesEarlierHandCard() {
        Card greenCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(greenCard, new Endurance()));
        harness.setGraveyard(player2, List.of(graveyardCard));

        harness.castInstantWithAlternateExileFromHand(player1, 1, (UUID) null, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(greenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        harness.assertInGraveyard(player1, "Endurance");
        harness.assertNotOnBattlefield(player1, "Endurance");
    }

    @Test
    @DisplayName("The controller can be selected as the target when the ETB trigger goes on the stack")
    void etbTargetSelectionCanChooseController() {
        Card graveyardCard = new GrizzlyBears();
        Card existingTop = new Forest();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(new Endurance()));
        giveHardcastMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTop, graveyardCard);
    }

    @Test
    @DisplayName("Its single-target trigger prompt can be declined even though Endurance's controller is a legal target")
    void singleTargetTriggerPromptCanBeDeclined() {
        Card ownGraveyardCard = new GrizzlyBears();
        Card opponentGraveyardCard = new Forest();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        openSingleTargetTriggerPrompt();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getTargetId()).isNull());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }

    @Test
    @DisplayName("Its single-target trigger prompt can target Endurance's controller")
    void singleTargetTriggerPromptCanTargetTheController() {
        Card ownGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setLibrary(player1, List.of());
        openSingleTargetTriggerPrompt();

        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownGraveyardCard);
    }

    /**
     * Opens the single-target "up to one target player" prompt that a trigger outside the ETB
     * walker (Equipment-attach, enters-from-graveyard) uses for Endurance's ability.
     */
    private void openSingleTargetTriggerPrompt() {
        Permanent endurance = harness.addToBattlefieldAndReturn(player1, new Endurance());
        Card card = endurance.getCard();
        gd.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(card, player1.getId(),
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD), endurance.getId(), card.getTargetFilter()));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(ETBTokenTargetService.class)
                .processNextETBTokenTargetTrigger(gd));

        var prompt = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(prompt.context()).isInstanceOf(MultiPermanentChoiceContext.EtbOptionalTarget.class);
        assertThat(prompt.validPlayerIds()).contains(player1.getId(), player2.getId());
        assertThat(prompt.maxCount()).isEqualTo(1);
    }

    private void giveHardcastMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
