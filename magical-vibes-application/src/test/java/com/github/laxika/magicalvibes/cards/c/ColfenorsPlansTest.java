package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColfenorsPlans.class, Forest.class, LeafGilder.class, Donate.class})
class ColfenorsPlansTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top 7 cards of the controller's library (only) to itself")
    void etbExilesTopSevenOfControllersLibrary() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 8; i++) library.add(new Forest());
        harness.setLibrary(player1, library);

        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new ColfenorsPlans(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve enchantment â†’ ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Colfenor's Plans");

        UUID permId = harness.getPermanentId(player1, "Colfenor's Plans");
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(7);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        // Opponent's library is untouched (this exiles only the controller's cards).
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new ColfenorsPlans());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP â†’ DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Controller can't cast a second spell in a turn")
    void controllerLimitedToOneSpell() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        harness.setHand(player1, List.of(new LeafGilder(), new LeafGilder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent is not restricted by Colfenor's Plans")
    void opponentNotRestricted() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        harness.setHand(player2, List.of(new LeafGilder(), new LeafGilder()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Second spell is fine for the opponent.
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Controller may play a land and cast a spell from the exiled cards")
    void playsAndCastsFromExile() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        UUID permId = harness.getPermanentId(player1, "Colfenor's Plans");

        Card exiledLand = new Forest();
        Card exiledCreature = new LeafGilder();
        gd.addToExile(player1.getId(), exiledLand, permId);
        gd.addToExile(player1.getId(), exiledCreature, permId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play the land from exile â€” no longer in exile, now on battlefield.
        harness.castFromExile(player1, exiledLand.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getCardsExiledByPermanent(permId))
                .noneMatch(c -> c.getId().equals(exiledLand.getId()));

        // Cast the creature from exile â€” put it on the stack and resolve it.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Leaf Gilder");
    }

    @Test
    @DisplayName("Controller may cast a face-down spell exiled with Colfenor's Plans")
    void castsFaceDownSpellExiledWithPlans() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        UUID permId = harness.getPermanentId(player1, "Colfenor's Plans");
        Card exiledCreature = new LeafGilder();
        gd.addToExile(player1.getId(), exiledCreature, permId, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leaf Gilder");
    }
    @Test
    @DisplayName("Skipping the draw step provides no draw-step priority window")
    void skipsTheEntireDrawStep() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.withAutoStop(TurnStep.DRAW, () -> harness.passBothPriorities()));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("ETB exiles all remaining cards when the library has fewer than seven")
    void exilesAShortLibrary() {
        Card first = new Forest();
        Card second = new LeafGilder();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new ColfenorsPlans(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Colfenor's Plans");
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting Plans itself uses the controller's spell allowance for that turn")
    void plansCountsAsTheSpellForItsTurn() {
        harness.setLibrary(player1, List.of(new LeafGilder()));
        harness.castFromHand(player1, new ColfenorsPlans(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LeafGilder()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        UUID sourceId = harness.getPermanentId(player1, "Colfenor's Plans");
        UUID cardId = gd.getCardsExiledByPermanent(sourceId).getFirst().getId();
        assertThatThrownBy(() -> harness.castFromExile(player1, cardId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiled spells still require their mana cost")
    void exilePermissionDoesNotWaiveManaCost() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        UUID sourceId = harness.getPermanentId(player1, "Colfenor's Plans");
        Card creature = new LeafGilder();
        gd.addToExile(player1.getId(), creature, sourceId, true);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactly(creature);
    }

    @Test
    @DisplayName("Exiled creatures still require normal sorcery timing")
    void exilePermissionDoesNotWaiveTiming() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        UUID sourceId = harness.getPermanentId(player1, "Colfenor's Plans");
        Card creature = new LeafGilder();
        gd.addToExile(player1.getId(), creature, sourceId, true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(sourceId)).containsExactly(creature);
    }

    @Test
    @CardUsed({ColfenorsPlans.class, Forest.class, LeafGilder.class, Donate.class})
    @DisplayName("A former controller retains permission to look at the exiled cards")
    void formerControllerCanStillLookAfterDonate() throws Exception {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card exiled = new LeafGilder();
        List<Card> library = new ArrayList<>();
        library.add(exiled);
        for (int i = 0; i < 7; i++) library.add(new Forest());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ColfenorsPlans(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Colfenor's Plans");
        harness.publishState();
        assertThat(plansView(harness.getConn1(), sourceId).faceDownExiledCards())
                .anyMatch(card -> card.id().equals(exiled.getId()));
        assertThat(plansView(harness.getConn2(), sourceId).faceDownExiledCards()).isEmpty();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), sourceId));
        harness.publishState();

        assertThat(plansView(harness.getConn2(), sourceId).faceDownExiledCards())
                .anyMatch(card -> card.id().equals(exiled.getId()));
        assertThat(plansView(harness.getConn1(), sourceId).faceDownExiledCards())
                .anyMatch(card -> card.id().equals(exiled.getId()));
    }

    private PermanentView plansView(FakeConnection connection, UUID sourceId) throws Exception {
        String message = connection.getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        GameStateMessage state = new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class);
        return state.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(sourceId)).findFirst().orElseThrow();
    }
}
