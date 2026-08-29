package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OxOfAgonas.class, GrizzlyBears.class, Island.class})
class OxOfAgonasTest extends BaseCardTest {

    @Test
    void enteringDiscardsHandAndDrawsThreeCards() {
        OxOfAgonas ox = new OxOfAgonas();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(ox, discarded));
        List<Card> drawn = List.of(new Island(), new Island(), new Island());
        harness.setLibrary(player1, drawn);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        Permanent enteredOx = findPermanent(player1, "Ox of Agonas");
        assertThat(enteredOx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesEightOtherCardsAndAddsCounter() {
        OxOfAgonas ox = new OxOfAgonas();
        List<GrizzlyBears> otherCards = IntStream.range(0, 8)
                .mapToObj(ignored -> new GrizzlyBears())
                .toList();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(ox);
        graveyard.addAll(otherCards);
        harness.setGraveyard(player1, graveyard);
        Island discarded = new Island();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromGraveyard(player1, 0, IntStream.rangeClosed(1, 8).boxed().toList());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escapedOx = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(escapedOx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void escapeRequiresEightOtherCardsInTheGraveyard() {
        OxOfAgonas ox = new OxOfAgonas();
        List<GrizzlyBears> otherCards = IntStream.range(0, 7)
                .mapToObj(ignored -> new GrizzlyBears())
                .toList();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(ox);
        graveyard.addAll(otherCards);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(
                player1, 0, IntStream.rangeClosed(1, 7).boxed().toList()))
                .isInstanceOf(IllegalStateException.class);
    }
}
