package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RealityShift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokGuide.class, Forest.class, Plains.class, RealityShift.class})
class AinokGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Ainok Guide")
    void counterModePutsCounterOnItself() {
        castGuide(List.of(new AinokGuide()));

        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Ainok Guide");
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Land mode searches for a basic land and puts it on top of the library")
    void landModePutsBasicLandOnTop() {
        castGuide(List.of(new Forest(), new Plains(), new AinokGuide()));

        harness.handleListChoice(player1,
                "Search your library for a basic land card, reveal it, then shuffle and put that card on top");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).isNotEmpty();
        assertThat(library.getFirst().getName()).isEqualTo("Forest");
        assertThat(library).anyMatch(card -> card instanceof AinokGuide);
    }

    @Test
    @DisplayName("Land mode still searches after Ainok Guide leaves the battlefield")
    void landModeResolvesWithoutGuide() {
        AinokGuide manifestedCard = new AinokGuide();
        Forest forest = new Forest();
        castGuide(List.of(manifestedCard, forest));
        harness.handleListChoice(player1,
                "Search your library for a basic land card, reveal it, then shuffle and put that card on top");

        harness.setHand(player1, List.of(new RealityShift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Ainok Guide").getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Land mode may fail to find even when a basic land is available")
    void landModeMayFailToFind() {
        Forest forest = new Forest();
        castGuide(List.of(forest));
        harness.handleListChoice(player1,
                "Search your library for a basic land card, reveal it, then shuffle and put that card on top");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(findPermanent(player1, "Ainok Guide").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Land mode resolves when the library contains no basic lands")
    void landModeWithNoBasicLands() {
        AinokGuide otherGuide = new AinokGuide();
        castGuide(List.of(otherGuide));
        harness.handleListChoice(player1,
                "Search your library for a basic land card, reveal it, then shuffle and put that card on top");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherGuide);
        assertThat(findPermanent(player1, "Ainok Guide").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
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
