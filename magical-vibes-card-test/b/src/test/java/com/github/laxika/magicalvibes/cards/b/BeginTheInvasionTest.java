package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.i.InvasionOfMoag;
import com.github.laxika.magicalvibes.cards.i.InvasionOfMuraganda;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeginTheInvasion.class, GrizzlyBears.class, RuneclawBear.class, Shock.class,
        InvasionOfMoag.class, InvasionOfMuraganda.class, DoublingSeason.class})
class BeginTheInvasionTest extends BaseCardTest {

    @Test
    void searchesForUpToXBattleCards() {
        Card firstBattle = battle(new GrizzlyBears());
        Card duplicateBattle = battle(new GrizzlyBears());
        Card secondBattle = battle(new RuneclawBear());
        Card nonBattle = new Shock();
        harness.setLibrary(player1, List.of(firstBattle, duplicateBattle, secondBattle, nonBattle));

        castBeginTheInvasion(2);

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().requireDifferentNames()).isTrue();
        assertThat(search.params().cards()).containsExactly(firstBattle, duplicateBattle, secondBattle);
    }

    @Test
    void putsDistinctBattleCardsOntoTheBattlefield() {
        Card firstBattle = battle(new GrizzlyBears());
        Card duplicateBattle = battle(new GrizzlyBears());
        Card secondBattle = battle(new RuneclawBear());
        Card nonBattle = new Shock();
        harness.setLibrary(player1, List.of(firstBattle, duplicateBattle, secondBattle, nonBattle));

        castBeginTheInvasion(2);

        chooseLibraryCard(firstBattle);
        assertThat(activeSearch().params().cards()).containsExactly(secondBattle);

        chooseLibraryCard(secondBattle);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard))
                .containsExactlyInAnyOrder(firstBattle, secondBattle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(duplicateBattle, nonBattle);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNothingWhenXIsZero() {
        Card battle = battle(new GrizzlyBears());
        Card nonBattle = new Shock();
        harness.setLibrary(player1, List.of(battle, nonBattle));

        castBeginTheInvasion(0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(battle, nonBattle);
    }

    @Test
    void realSiegesEnterTogetherWithDefenseAndOpponentProtectors() {
        Card firstBattle = new InvasionOfMoag();
        Card secondBattle = new InvasionOfMuraganda();
        harness.setLibrary(player1, List.of(firstBattle, secondBattle));

        castBeginTheInvasion(2);
        chooseLibraryCard(firstBattle);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        chooseLibraryCard(secondBattle);

        List<Permanent> battles = gd.playerBattlefields.get(player1.getId());
        assertThat(battles).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(firstBattle, secondBattle);
        assertThat(battles).allSatisfy(permanent -> {
            assertThat(permanent.isTapped()).isFalse();
            assertThat(permanent.getProtectorPlayerId()).isEqualTo(player2.getId());
        });
        assertThat(battles.stream().filter(p -> p.getCard() == firstBattle).findFirst().orElseThrow()
                .getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        assertThat(battles.stream().filter(p -> p.getCard() == secondBattle).findFirst().orElseThrow()
                .getCounterCount(CounterType.DEFENSE)).isEqualTo(6);
    }

    @Test
    void defenseCountersRespectDoublingSeason() {
        harness.addToBattlefield(player1, new DoublingSeason());
        Card battle = new InvasionOfMoag();
        harness.setLibrary(player1, List.of(battle));

        castBeginTheInvasion(1);
        chooseLibraryCard(battle);

        Permanent enteredBattle = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == battle).findFirst().orElseThrow();
        assertThat(enteredBattle.getCounterCount(CounterType.DEFENSE)).isEqualTo(10);
    }

    @Test
    void mayStopAfterFindingFewerThanXBattles() {
        Card firstBattle = new InvasionOfMoag();
        Card secondBattle = new InvasionOfMuraganda();
        harness.setLibrary(player1, List.of(firstBattle, secondBattle));

        castBeginTheInvasion(3);
        chooseLibraryCard(firstBattle);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactly(firstBattle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondBattle);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseNoBattlesEvenWhenOneIsAvailable() {
        Card battle = new InvasionOfMoag();
        harness.setLibrary(player1, List.of(battle));

        castBeginTheInvasion(2);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(battle);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void finishesWhenLibraryContainsNoBattles() {
        Card nonBattle = new BeginTheInvasion();
        harness.setLibrary(player1, List.of(nonBattle));

        castBeginTheInvasion(2);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBattle);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBeginTheInvasion(int xValue) {
        harness.setHand(player1, List.of(new BeginTheInvasion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseLibraryCard(Card card) {
        PendingInteraction.LibrarySearch search = activeSearch();
        harness.handleCardChosen(player1, search.params().cards().indexOf(card));
    }

    private Card battle(Card card) {
        card.setType(CardType.BATTLE);
        card.setDefense(5);
        return card;
    }
}
