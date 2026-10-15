package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureKeeper.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Plains.class, WrathOfGod.class})
class TreasureKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger reveals until the first nonland card with mana value 3 or less")
    void revealsUntilFirstQualifyingCard() {
        TreasureKeeper keeper = new TreasureKeeper();
        HillGiant tooExpensive = new HillGiant();
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        GrizzlyBears belowHit = new GrizzlyBears();
        setUpDeathTrigger(keeper, List.of(new Plains(), tooExpensive, hit, belowHit));

        resolveDeathTrigger();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("The qualifying card may be cast without paying its mana cost")
    void castsQualifyingCardForFree() {
        TreasureKeeper keeper = new TreasureKeeper();
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        setUpDeathTrigger(keeper, List.of(hit));
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        resolveDeathTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore);
    }

    @Test
    @DisplayName("If no qualifying card is found, all revealed cards go to the library bottom")
    void bottomsRevealedCardsWhenNoMatch() {
        TreasureKeeper keeper = new TreasureKeeper();
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        setUpDeathTrigger(keeper, List.of(land, tooExpensive));

        resolveDeathTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Declining bottoms every revealed card without disturbing the unrevealed library")
    void decliningBottomsAllRevealedCards() {
        Plains land = new Plains();
        HillGiant expensive = new HillGiant();
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        GrizzlyBears unrevealed = new GrizzlyBears();
        setUpDeathTrigger(new TreasureKeeper(), List.of(land, expensive, hit, unrevealed));

        resolveDeathTrigger();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(land, expensive, hit);
    }

    @Test
    @DisplayName("A revealed creature is cast and resolves normally while skipped cards are bottomed")
    void castsCreatureAndBottomsSkippedCards() {
        Plains land = new Plains();
        GrizzlyBears hit = new GrizzlyBears();
        Forest unrevealed = new Forest();
        setUpDeathTrigger(new TreasureKeeper(), List.of(land, hit, unrevealed));

        resolveDeathTrigger();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, land);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library produces no choice and no spell")
    void emptyLibraryDoesNothing() {
        setUpDeathTrigger(new TreasureKeeper(), List.of());

        resolveDeathTrigger();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Skipped cards are publicly revealed as well as the qualifying card")
    void publiclyRevealsSkippedCards() {
        setUpDeathTrigger(new TreasureKeeper(),
                List.of(new Plains(), new HillGiant(), new GrizzlyBears()));

        resolveDeathTrigger();

        assertThat(gameLogContains("Plains")).isTrue();
        assertThat(gameLogContains("Hill Giant")).isTrue();
        assertThat(gameLogContains("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Every card is publicly revealed when no qualifying card is found")
    void publiclyRevealsCardsWithoutMatch() {
        setUpDeathTrigger(new TreasureKeeper(), List.of(new Forest(), new HillGiant()));

        resolveDeathTrigger();

        assertThat(gameLogContains("Forest")).isTrue();
        assertThat(gameLogContains("Hill Giant")).isTrue();
    }

    private void setUpDeathTrigger(TreasureKeeper keeper, List<Card> library) {
        harness.addToBattlefield(player1, keeper);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
    }

    private void resolveDeathTrigger() {
        resolveAllTriggers();
    }
}
