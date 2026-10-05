package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoryDrain.class, NyxbornColossus.class})
class MemoryDrainTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell and scries 2")
    void countersTargetSpellAndScriesTwo() {
        NyxbornColossus bears = new NyxbornColossus();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top0, top1);

        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck).startsWith(top1, top0);
        harness.assertInGraveyard(player2, "Memory Drain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, harness.getPermanentId(player1, "Nyxborn Colossus")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutBothScryCardsOnBottomInChosenOrder() {
        NyxbornColossus creature = new NyxbornColossus();
        MemoryDrain first = new MemoryDrain();
        MemoryDrain second = new MemoryDrain();
        MemoryDrain third = new MemoryDrain();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, second, first);
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player2, "Memory Drain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scriesOnlyAvailableCardInOneCardLibrary() {
        NyxbornColossus creature = new NyxbornColossus();
        MemoryDrain top = new MemoryDrain();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player2, "Memory Drain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotScryWhenTargetHasAlreadyBeenCountered() {
        NyxbornColossus creature = new NyxbornColossus();
        MemoryDrain top = new MemoryDrain();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new MemoryDrain(), new MemoryDrain()));
        harness.addMana(player2, ManaColor.BLUE, 8);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
