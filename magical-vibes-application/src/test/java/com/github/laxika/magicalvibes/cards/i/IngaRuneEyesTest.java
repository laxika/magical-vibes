package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.d.Doomskar;
import com.github.laxika.magicalvibes.cards.s.ScornEffigy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({IngaRuneEyes.class, DemonBolt.class, Doomskar.class, ScornEffigy.class})
class IngaRuneEyesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a scry 3 trigger")
    void entersWithScryThree() {
        Card top = new Doomskar();
        Card middle = new ScornEffigy();
        Card bottom = new Doomskar();
        Card next = new ScornEffigy();
        harness.setLibrary(player1, List.of(top, middle, bottom, next));
        harness.setHand(player1, List.of(new IngaRuneEyes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, middle, bottom);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(middle, next, top, bottom);
    }

    @Test
    @DisplayName("Draws three cards when three creatures die before the trigger resolves")
    void deathTriggerCountsCreaturesDyingWhileItWaits() {
        harness.addToBattlefield(player1, new IngaRuneEyes());
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new ScornEffigy()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player2, new ScornEffigy()).getId();
        harness.setHand(player1, List.of(new DemonBolt(), new DemonBolt(), new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        UUID ingaId = harness.getPermanentId(player1, "Inga Rune-Eyes");

        harness.castAndResolveInstant(player1, 0, ingaId);
        harness.assertInGraveyard(player1, "Inga Rune-Eyes");
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, firstId);
        harness.castAndResolveInstant(player1, 0, secondId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures died this turn")
    void deathTriggerRequiresThreeCreatures() {
        harness.addToBattlefield(player1, new IngaRuneEyes());
        harness.addToBattlefield(player1, new ScornEffigy());
        harness.setHand(player1, List.of(new Doomskar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    void countsIngaAndOpponentsCreaturesDyingSimultaneously() {
        harness.addToBattlefield(player1, new IngaRuneEyes());
        harness.addToBattlefield(player2, new ScornEffigy());
        harness.addToBattlefield(player2, new ScornEffigy());
        harness.setHand(player1, List.of(new Doomskar()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void countsCreaturesThatDiedBeforeIngaEntered() {
        harness.setLibrary(player1, List.of(new ScornEffigy(), new ScornEffigy(), new ScornEffigy()));
        UUID firstId = harness.addToBattlefieldAndReturn(player2, new ScornEffigy()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player2, new ScornEffigy()).getId();
        harness.setHand(player1, List.of(new DemonBolt(), new DemonBolt(),
                new IngaRuneEyes(), new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castAndResolveInstant(player1, 0, firstId);
        harness.castAndResolveInstant(player1, 0, secondId);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Inga Rune-Eyes"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void scriesOnlyAvailableCardsWithShortLibrary() {
        Card first = new ScornEffigy();
        Card second = new DemonBolt();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new IngaRuneEyes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void scryWithEmptyLibraryCompletesWithoutDrawingOrPrompting() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new IngaRuneEyes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inga Rune-Eyes");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
