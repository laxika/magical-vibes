package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
