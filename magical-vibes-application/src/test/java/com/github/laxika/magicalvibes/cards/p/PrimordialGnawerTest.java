package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.q.QuintoriusKand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimordialGnawer.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, WrathOfGod.class, QuintoriusKand.class})
class PrimordialGnawerTest extends BaseCardTest {

    @Test
    @DisplayName("When Primordial Gnawer dies, discover 3")
    void discoversThreeWhenItDies() {
        PrimordialGnawer gnawer = new PrimordialGnawer();
        HillGiant tooExpensive = new HillGiant();
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        GrizzlyBears belowDiscovered = new GrizzlyBears();
        Forest land = new Forest();
        setUpDeathTrigger(gnawer, List.of(land, tooExpensive, discovered, belowDiscovered));

        resolveDeathTrigger();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive,
                belowDiscovered);
    }

    @Test
    @DisplayName("Discover 3 can cast the found card without paying its mana cost")
    void castsDiscoveredCardForFree() {
        PrimordialGnawer gnawer = new PrimordialGnawer();
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        setUpDeathTrigger(gnawer, List.of(discovered));
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        resolveDeathTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore);
    }

    @Test
    @DisplayName("Skipped cards go below the untouched library after discovering")
    void putsSkippedCardsBelowUntouchedLibrary() {
        Forest land = new Forest();
        HillGiant expensive = new HillGiant();
        GrizzlyBears discovered = new GrizzlyBears();
        CounselOfTheSoratami untouched = new CounselOfTheSoratami();
        setUpDeathTrigger(new PrimordialGnawer(), List.of(land, expensive, discovered, untouched));

        resolveDeathTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
    }

    @Test
    @DisplayName("Discover returns all cards when no qualifying nonland card exists")
    void returnsLibraryWhenNoCardQualifies() {
        Forest land = new Forest();
        HillGiant expensive = new HillGiant();
        setUpDeathTrigger(new PrimordialGnawer(), List.of(land, expensive));

        resolveDeathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discovering with an empty library finishes without drawing")
    void finishesWithEmptyLibrary() {
        setUpDeathTrigger(new PrimordialGnawer(), List.of());

        resolveDeathTrigger();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell cast by discover triggers Quintorius Kand")
    void discoveredSpellIsCastFromExile() {
        harness.addToBattlefield(player1, new QuintoriusKand());
        setUpDeathTrigger(new PrimordialGnawer(), List.of(new GrizzlyBears()));

        resolveDeathTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void setUpDeathTrigger(PrimordialGnawer gnawer, List<Card> library) {
        harness.addToBattlefield(player1, gnawer);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
    }

    private void resolveDeathTrigger() {
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
