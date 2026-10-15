package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.cards.r.ReinsOfPower;
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

@CardUsed({JadelightRanger.class, Forest.class, MomentOfCraving.class, ReinsOfPower.class})
class JadelightRangerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB explores twice and puts both revealed lands into hand")
    void exploresTwiceWithLands() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        castJadelightRanger();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstLand.getId(), secondLand.getId());
        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB explores twice and adds a counter for each revealed nonland")
    void exploresTwiceWithNonlands() {
        Card firstNonland = new JadelightRanger();
        Card secondNonland = new JadelightRanger();
        harness.setLibrary(player1, List.of(firstNonland, secondNonland));

        castJadelightRanger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstNonland.getId(), secondNonland.getId());
    }

    @Test
    @DisplayName("Keeping a nonland reveals the same card for the second explore")
    void exploresSameNonlandTwiceWhenKept() {
        Card nonland = new JadelightRanger();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of(nonland, nextCard));

        castJadelightRanger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Graveyarding a nonland lets the second explore put the next land into hand")
    void exploresNonlandThenLand() {
        Card nonland = new JadelightRanger();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));

        castJadelightRanger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A land followed by a nonland gives a card and one counter")
    void exploresLandThenNonland() {
        Card land = new Forest();
        Card nonland = new JadelightRanger();
        harness.setLibrary(player1, List.of(land, nonland));

        castJadelightRanger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exploring an empty library twice still gives two counters")
    void exploresEmptyLibraryTwice() {
        harness.setLibrary(player1, List.of());

        castJadelightRanger();

        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second explore gives a counter when the first takes the last land")
    void exploresAfterTakingLastLand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        castJadelightRanger();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(findJadelightRanger().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both explores still happen if Ranger leaves before its trigger resolves")
    void exploresTwiceAfterLeavingBattlefield() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new JadelightRanger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, findJadelightRanger().getId());
        harness.assertNotOnBattlefield(player1, "Jadelight Ranger");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exploration uses Ranger's current controller after control changes")
    void exploresCurrentControllersLibrary() {
        Card originalControllersLand = new Forest();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(originalControllersLand));
        harness.setLibrary(player2, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new JadelightRanger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new ReinsOfPower()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertOnBattlefield(player2, "Jadelight Ranger");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalControllersLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(originalControllersLand);
        assertThat(findPermanent(player2, "Jadelight Ranger")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castJadelightRanger() {
        harness.setHand(player1, List.of(new JadelightRanger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent findJadelightRanger() {
        return findPermanent(player1, "Jadelight Ranger");
    }
}
