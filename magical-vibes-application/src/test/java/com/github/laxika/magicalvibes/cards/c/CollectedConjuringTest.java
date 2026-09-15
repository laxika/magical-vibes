package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CollectedConjuring.class, CrashingFootfalls.class, DreadReturn.class,
        Forest.class, GrizzlyBears.class, Shock.class})
class CollectedConjuringTest extends BaseCardTest {

    @Test
    void offersUpToTwoSorceriesWithManaValueThreeOrLess() {
        CrashingFootfalls first = new CrashingFootfalls();
        CrashingFootfalls second = new CrashingFootfalls();
        Forest leftover = new Forest();
        List<Card> library = List.of(
                first, new Shock(), second, new GrizzlyBears(), new Forest(), new DreadReturn(), leftover);
        cast(library);

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        library.get(1).getId(), library.get(3).getId(), library.get(4).getId(),
                        library.get(5).getId(), library.get(6).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(leftover);
    }

    @Test
    void putsAllCardsOnTheBottomWhenNoEligibleSorceryIsFound() {
        List<Card> library = List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Forest(), new Forest(), new Forest(),
                new Forest());
        cast(library);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CollectedConjuring()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
