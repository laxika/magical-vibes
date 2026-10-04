package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SkophosMazeWarden;
import com.github.laxika.magicalvibes.cards.s.SkophosWarleader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        DeathbellowWarCry.class,
        FelhideBrawler.class,
        GrizzlyBears.class,
        Plains.class,
        SkophosMazeWarden.class,
        SkophosWarleader.class
})
class DeathbellowWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to four Minotaur creature cards with different names")
    void offersMinotaursWithDifferentNames() {
        Card mazeWarden = new SkophosMazeWarden();
        Card warleader = new SkophosWarleader();
        Card brawler = new FelhideBrawler();
        Card duplicateBrawler = new FelhideBrawler();
        Card bears = new GrizzlyBears();
        Card plains = new Plains();
        setLibrary(mazeWarden, warleader, brawler, duplicateBrawler, bears, plains);

        castDeathbellowWarCry();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(mazeWarden, warleader, brawler, duplicateBrawler);
        assertThat(search.params().remainingCount()).isEqualTo(4);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().requireDifferentNames()).isTrue();

        chooseFromLibrary(brawler);
        assertThat(activeSearch().params().cards()).doesNotContain(duplicateBrawler);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("Puts up to four different-named Minotaurs onto the battlefield")
    void putsDifferentNamedMinotaursOntoBattlefield() {
        Card mazeWarden = new SkophosMazeWarden();
        Card warleader = new SkophosWarleader();
        Card fourthMinotaur = new Card();
        fourthMinotaur.setName("Fourth Minotaur");
        fourthMinotaur.setType(CardType.CREATURE);
        fourthMinotaur.setSubtypes(List.of(CardSubtype.MINOTAUR));
        fourthMinotaur.setPower(2);
        fourthMinotaur.setToughness(2);
        Card felhideBrawler = new FelhideBrawler();
        setLibrary(mazeWarden, warleader, fourthMinotaur, felhideBrawler);

        castDeathbellowWarCry();
        harness.passBothPriorities();

        chooseFromLibrary(mazeWarden);
        chooseFromLibrary(warleader);
        chooseFromLibrary(fourthMinotaur);
        chooseFromLibrary(felhideBrawler);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactlyInAnyOrder(mazeWarden, warleader, fourthMinotaur, felhideBrawler);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May choose fewer than four Minotaurs")
    void mayChooseFewerThanFourMinotaurs() {
        Card mazeWarden = new SkophosMazeWarden();
        Card warleader = new SkophosWarleader();
        setLibrary(mazeWarden, warleader);

        castDeathbellowWarCry();
        harness.passBothPriorities();
        chooseFromLibrary(mazeWarden);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .containsExactly(mazeWarden);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(warleader);
    }

    private void castDeathbellowWarCry() {
        harness.setHand(player1, List.of(new DeathbellowWarCry()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void chooseFromLibrary(Card card) {
        List<Card> offeredCards = activeSearch().params().cards();
        int index = offeredCards.indexOf(card);
        assertThat(index).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
