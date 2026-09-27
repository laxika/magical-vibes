package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
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

@CardUsed({MuseVortex.class, Cancel.class, Forest.class, GrizzlyBears.class, Ponder.class})
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

    private void cast(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MuseVortex()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
