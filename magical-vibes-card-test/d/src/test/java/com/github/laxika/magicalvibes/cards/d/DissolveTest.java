package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SpearpointOread;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dissolve.class, SpearpointOread.class})
class DissolveTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and then lets its controller scry 1")
    void countersSpellAndScries() {
        SpearpointOread bears = new SpearpointOread();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new Dissolve()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Spearpoint Oread");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Completing Dissolve's scry finishes resolving the spell")
    void completingScryFinishesResolution() {
        SpearpointOread bears = new SpearpointOread();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new Dissolve()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card originalTop = deck.get(0);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Dissolve");
    }

    @Test
    void canKeepTheTopCard() {
        SpearpointOread creature = new SpearpointOread();
        Dissolve top = new Dissolve();
        Dissolve bottom = new Dissolve();
        harness.setLibrary(player2, List.of(top, bottom));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Dissolve()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom);
        harness.assertInGraveyard(player1, "Spearpoint Oread");
        harness.assertInGraveyard(player2, "Dissolve");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventCounteringOrFinishingResolution() {
        SpearpointOread creature = new SpearpointOread();
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Dissolve()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Spearpoint Oread");
        harness.assertInGraveyard(player2, "Dissolve");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotScryWhenTheTargetWasAlreadyCountered() {
        SpearpointOread creature = new SpearpointOread();
        Dissolve top = new Dissolve();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Dissolve(), new Dissolve()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
