package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AinokGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Ainok Guide")
    void counterModePutsCounterOnItself() {
        castGuide(List.of(new GrizzlyBears()));

        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Ainok Guide");
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Land mode searches for a basic land and puts it on top of the library")
    void landModePutsBasicLandOnTop() {
        castGuide(List.of(new Forest(), new Plains(), new GrizzlyBears()));

        harness.handleListChoice(player1,
                "Search your library for a basic land card, reveal it, then shuffle and put that card on top");
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).isNotEmpty();
        assertThat(library.getFirst().getName()).isEqualTo("Forest");
        assertThat(library).anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void castGuide(List<Card> library) {
        harness.setHand(player1, List.of(new AinokGuide()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
