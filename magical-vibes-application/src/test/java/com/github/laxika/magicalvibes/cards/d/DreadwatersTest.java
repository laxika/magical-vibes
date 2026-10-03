package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadwaters.class, Island.class})
class DreadwatersTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills one card per land the caster controls")
    void millsForEachLandYouControl() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        harness.setLibrary(player2, IntStream.range(0, 10).mapToObj(i -> new Island()).toList());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills nothing when the caster controls no lands")
    void millsNothingWithoutLands() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setLibrary(player2, IntStream.range(0, 10).mapToObj(i -> new Island()).toList());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        harness.setLibrary(player1, IntStream.range(0, 10).mapToObj(i -> new Island()).toList());

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Two milled cards plus Dreadwaters itself.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Counts lands at resolution rather than when cast")
    void countsLandsAtResolution() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        Island topCard = new Island();
        Island secondCard = new Island();
        Island remainingCard = new Island();
        harness.setLibrary(player2, List.of(topCard, secondCard, remainingCard));

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard, secondCard);
    }

    @Test
    @DisplayName("Mills all available cards when the library has fewer cards than lands")
    void millsOnlyAvailableCards() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        Island onlyCard = new Island();
        harness.setLibrary(player2, List.of(onlyCard));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
    }
}
