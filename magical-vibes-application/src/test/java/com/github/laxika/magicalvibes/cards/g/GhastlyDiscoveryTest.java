package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhastlyDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then the controller discards one")
    void drawsTwoThenDiscardsOne() {
        setDeck(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        // Two cards drawn, now awaiting the discard choice.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // The discarded card lands in the graveyard (alongside the resolved sorcery itself).
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        setDeck(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent wizardA = addCreatureReady(player1, new FugitiveWizard());
        Permanent wizardB = addCreatureReady(player1, new FugitiveWizard());

        harness.castWithConspire(player1, 0, null, List.of(wizardA.getId(), wizardB.getId()));

        assertThat(wizardA.isTapped()).isTrue();
        assertThat(wizardB.isTapped()).isTrue();

        // The spell plus one conspire copy trigger are on the stack.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsColorlessCreature() {
        setDeck(player1, List.of(new GrizzlyBears(), new Ornithopter()));
        harness.setHand(player1, List.of(new GhastlyDiscovery()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent thopter = addCreatureReady(player1, new Ornithopter()); // colorless

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(wizard.getId(), thopter.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
