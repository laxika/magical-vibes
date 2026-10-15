package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuseVortex.class, Cancel.class, Forest.class, GrizzlyBears.class, Ponder.class,
        BeanstalkGiant.class, FertileFootsteps.class})
class MuseVortexTest extends BaseCardTest {

    @Test
    void offersOneInstantOrSorceryWithManaValueAtMostX() {
        Ponder ponder = new Ponder();
        Cancel cancel = new Cancel();
        List<Card> library = List.of(ponder, cancel, new GrizzlyBears(), new Forest(), new Forest());
        cast(4, library);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ponder.getId(), cancel.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(library.subList(0, 4).stream().map(Card::getId).toList());
    }

    @Test
    void putsUncastInstantsAndSorceriesIntoHandAndEverythingElseOnBottom() {
        Ponder ponder = new Ponder();
        Cancel cancel = new Cancel();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Forest leftover = new Forest();
        cast(4, List.of(ponder, cancel, bears, forest, leftover));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(ponder, cancel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, forest, leftover);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void putsAllCardsOnTheBottomWhenNoInstantOrSorceryQualifies() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Forest leftover = new Forest();
        cast(3, List.of(bears, forest, leftover));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, forest, leftover);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void castsOneSpellForFreeAndReturnsTheOtherRemaindersToTheirZones() {
        Ponder ponder = new Ponder();
        Cancel cancel = new Cancel();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Forest leftover = new Forest();
        cast(4, List.of(ponder, cancel, bears, forest, leftover));

        harness.handleMultipleCardsChosen(player1, List.of(ponder.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == ponder);
        assertThat(gd.playerHands.get(player1.getId())).contains(cancel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, forest, leftover);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void returnsSpellsAboveXToHandWhenNoneCanBeCast() {
        Cancel cancel = new Cancel();
        Forest forest = new Forest();
        cast(1, List.of(cancel, forest));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cancel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void excludesSpellsAboveXFromTheOfferButStillReturnsThemToHand() {
        Ponder ponder = new Ponder();
        Cancel cancel = new Cancel();
        Forest forest = new Forest();
        cast(2, List.of(ponder, cancel, forest));

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ponder.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(ponder, cancel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void leavesTheLibraryUntouchedWhenXIsZero() {
        Ponder ponder = new Ponder();
        Forest forest = new Forest();
        cast(0, List.of(ponder, forest));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ponder, forest);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void exilesOnlyAvailableCardsWhenXExceedsTheLibrarySize() {
        Ponder ponder = new Ponder();
        Forest forest = new Forest();
        cast(5, List.of(ponder, forest));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ponder);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void keepsUnexiledCardsAboveTheRandomlyBottomedCards() {
        Forest bottomed = new Forest();
        Forest untouched = new Forest();
        Ponder ponder = new Ponder();
        cast(1, List.of(bottomed, untouched, ponder));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, ponder, bottomed);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void castsAnotherMuseVortexWithXZeroWithoutExilingMoreCards() {
        MuseVortex nested = new MuseVortex();
        Forest bottomed = new Forest();
        Forest untouched = new Forest();
        cast(2, List.of(nested, bottomed, untouched));

        harness.handleMultipleCardsChosen(player1, List.of(nested.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == nested);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, bottomed);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, bottomed);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nested);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void offersAnAdventureUsingTheSorceryFacesManaValue() {
        BeanstalkGiant giant = new BeanstalkGiant();
        cast(3, List.of(giant, new Forest(), new Forest(), new Forest()));

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(giant.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    private void cast(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MuseVortex()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
