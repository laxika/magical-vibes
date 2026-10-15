package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cursecatcher;
import com.github.laxika.magicalvibes.cards.s.Scuttlemutt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhastlyDiscovery.class, Cursecatcher.class, Scuttlemutt.class})
class GhastlyDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then the controller discards one")
    void drawsTwoThenDiscardsOne() {
        harness.setLibrary(player1, List.of(new Cursecatcher(), new Scuttlemutt()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        // Two cards drawn, now awaiting the discard choice.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // The discarded card lands in the graveyard (alongside the resolved sorcery itself).
        harness.assertInGraveyard(player1, "Cursecatcher");
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setLibrary(player1, List.of(new Cursecatcher(), new Scuttlemutt()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent wizardA = addCreatureReady(player1, new Cursecatcher());
        Permanent wizardB = addCreatureReady(player1, new Cursecatcher());

        harness.castWithConspire(player1, 0, null, List.of(wizardA.getId(), wizardB.getId()));

        assertThat(wizardA.isTapped()).isTrue();
        assertThat(wizardB.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsColorlessCreature() {
        harness.setLibrary(player1, List.of(new Cursecatcher(), new Scuttlemutt()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent wizard = addCreatureReady(player1, new Cursecatcher());
        Permanent scarecrow = addCreatureReady(player1, new Scuttlemutt()); // colorless

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(wizard.getId(), scarecrow.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Conspire copy resolves the complete draw-then-discard sequence")
    void conspireCopyResolvesDrawAndDiscard() {
        harness.setLibrary(player1, List.of(
                new Cursecatcher(), new Scuttlemutt(), new Cursecatcher(), new Scuttlemutt()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent wizardA = addCreatureReady(player1, new Cursecatcher());
        Permanent wizardB = addCreatureReady(player1, new Cursecatcher());

        harness.castWithConspire(player1, 0, null, List.of(wizardA.getId(), wizardB.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The discarded card may have been in hand before the spell resolved")
    void canDiscardCardAlreadyInHand() {
        Scuttlemutt existingCard = new Scuttlemutt();
        Cursecatcher firstDraw = new Cursecatcher();
        Cursecatcher secondDraw = new Cursecatcher();
        harness.setHand(player1, List.of(new GhastlyDiscovery(), existingCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existingCard, firstDraw, secondDraw);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(existingCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay conspire and the copy does not conspire again")
    void summoningSickCreaturesCanConspire() {
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.setLibrary(player1, List.of(
                new Cursecatcher(), new Scuttlemutt(), new Cursecatcher(), new Scuttlemutt()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Cursecatcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Cursecatcher());

        harness.castWithConspire(player1, 0, null, List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof GhastlyDiscovery)).hasSize(1);
    }

    @Test
    @DisplayName("Conspire cannot tap the same creature twice")
    void conspireRejectsDuplicateCreature() {
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent creature = addCreatureReady(player1, new Cursecatcher());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Conspire cannot use an already tapped creature")
    void conspireRejectsTappedCreature() {
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent first = addCreatureReady(player1, new Cursecatcher());
        Permanent second = addCreatureReady(player1, new Cursecatcher());
        second.tap();

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Conspire cannot use an opponent's creature")
    void conspireRejectsOpponentsCreature() {
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent first = addCreatureReady(player1, new Cursecatcher());
        Permanent second = addCreatureReady(player2, new Cursecatcher());

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }
}
