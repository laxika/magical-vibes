package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MalametBrawler;
import com.github.laxika.magicalvibes.cards.a.AncestralReminiscence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Defossilize.class, Forest.class, MalametBrawler.class, AncestralReminiscence.class})
class DefossilizeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and explores it twice")
    void returnsCreatureAndExploresTwice() {
        Card creature = new MalametBrawler();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(firstLand, secondLand));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        var returned = findPermanent(player1, creature.getName());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(firstLand.getId(), secondLand.getId());
    }

    @Test
    @DisplayName("The returned creature gets a counter from each nonland explore")
    void returnedCreatureGetsCountersFromBothExplores() {
        Card creature = new MalametBrawler();
        Card firstNonland = new MalametBrawler();
        Card secondNonland = new MalametBrawler();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(firstNonland, secondNonland));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(firstNonland.getId(), secondNonland.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new AncestralReminiscence();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Putting the first revealed nonland into the graveyard exposes the next land")
    void graveyardChoiceChangesSecondExplore() {
        Card creature = new MalametBrawler();
        Card nonland = new AncestralReminiscence();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(nonland, land));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(nonland.getId()).doesNotContain(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(land.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both explores put counters on the returned creature when the library is empty")
    void exploresEmptyLibraryTwice() {
        Card creature = new MalametBrawler();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playersWhoControlledPermanentThatExploredThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("The second explore adds a counter after the last land goes to hand")
    void secondExploreAfterLastLand() {
        Card creature = new MalametBrawler();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Cannot target a creature in the opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new MalametBrawler();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unavailable target prevents returning and exploring")
    void removedTargetPreventsExploring() {
        Card creature = new MalametBrawler();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Defossilize()));
        addMana();

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId).containsExactly(land.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

}
