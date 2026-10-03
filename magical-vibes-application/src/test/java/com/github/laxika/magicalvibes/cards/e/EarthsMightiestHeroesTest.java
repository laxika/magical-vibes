package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthsMightiestHeroes.class, GrizzlyBears.class, Shock.class})
class EarthsMightiestHeroesTest extends BaseCardTest {

    @Test
    void putsUpToOneCreatureOntoTheBattlefieldWithoutTeamwork() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        Shock shock4 = new Shock();
        Shock shock5 = new Shock();
        setLibrary(first, shock1, second, shock2, shock3, shock4, shock5);

        cast(List.of());

        LibrarySearch search = gd.interaction.activeInteraction(LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, second);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().restToGraveyard()).isTrue();
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .extracting(Permanent::getCard).isSameAs(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock1, shock2, shock3, shock4, shock5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    void putsAnyNumberOfCreaturesOntoTheBattlefieldWithTeamwork() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        Permanent teammate1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate3 = addCreatureReady(player1, new GrizzlyBears());
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        Shock shock4 = new Shock();
        Shock shock5 = new Shock();
        setLibrary(first, shock1, second, shock2, third, shock3, shock4, shock5);

        cast(List.of(teammate1.getId(), teammate2.getId(), teammate3.getId()));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock1, shock2, shock3, shock4, shock5);
        assertThat(teammate1.isTapped()).isTrue();
        assertThat(teammate2.isTapped()).isTrue();
        assertThat(teammate3.isTapped()).isTrue();
    }

    @Test
    void mayDeclineTheCreatureWithoutTeamwork() {
        GrizzlyBears creature = new GrizzlyBears();
        Shock other = new Shock();
        setLibrary(creature, other);

        cast(List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, other);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayChooseOnlySomeCreaturesWithTeamwork() {
        Permanent teammate1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate3 = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears selected = new GrizzlyBears();
        GrizzlyBears declined = new GrizzlyBears();
        Shock other = new Shock();
        setLibrary(selected, declined, other);

        cast(List.of(teammate1.getId(), teammate2.getId(), teammate3.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .contains(selected).doesNotContain(declined);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(declined, other)
                .doesNotContain(selected);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard() == selected)
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isFalse());
    }

    @Test
    void mayChooseNoCreaturesWithTeamwork() {
        Permanent teammate1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent teammate3 = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        setLibrary(first, second);

        cast(List.of(teammate1.getId(), teammate2.getId(), teammate3.getId()));
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void onlyRevealsTheTopEightCards() {
        GrizzlyBears ninth = new GrizzlyBears();
        List<Card> revealed = List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, java.util.stream.Stream.concat(revealed.stream(), java.util.stream.Stream.of(ninth))
                .toList());

        cast(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ninth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(revealed).doesNotContain(ninth);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsTeamworkBelowFiveTotalPower() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Earth's Mightiest Heroes");
    }

    @Test
    void summoningSickCreaturesCanPayTeamwork() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        setLibrary();

        cast(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Earth's Mightiest Heroes");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        setLibrary();

        cast(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Earth's Mightiest Heroes");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void cast(List<java.util.UUID> teamworkPermanents) {
        harness.setHand(player1, List.of(new EarthsMightiestHeroes()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorceryWithSacrifices(player1, 0, null, teamworkPermanents);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
