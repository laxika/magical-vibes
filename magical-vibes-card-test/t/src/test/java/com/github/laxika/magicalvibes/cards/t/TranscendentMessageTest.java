package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ExpeditionLookout;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentMessage.class, GrizzlyBears.class, ExpeditionLookout.class})
class TranscendentMessageTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards")
    void drawsXCards() {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new TranscendentMessage()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(2));
    }

    @Test
    void zeroXDrawsNoCards() {
        Card libraryCard = new TranscendentMessage();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new TranscendentMessage()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Transcendent Message");
    }

    @Test
    void convokePaysBlueAndXCostsWithSummoningSickCreatures() {
        List<Card> library = List.of(new TranscendentMessage(), new TranscendentMessage(),
                new TranscendentMessage());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new TranscendentMessage()));
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new ExpeditionLookout()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 2, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(2));
        harness.assertInGraveyard(player1, "Transcendent Message");
    }

    @Test
    void greenCreatureCannotConvokeBlueMana() {
        harness.setHand(player1, List.of(new TranscendentMessage()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);
    }
}
