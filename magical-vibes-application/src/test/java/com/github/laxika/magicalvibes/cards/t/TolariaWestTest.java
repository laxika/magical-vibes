package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolariaWest.class, Memnite.class, GrizzlyBears.class})
class TolariaWestTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new TolariaWest()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tolaria West").isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TolariaWest());
        land.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void transmuteSearchesForManaValueZeroCard() {
        Memnite matchingCard = new Memnite();
        GrizzlyBears nonMatchingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TolariaWest()));
        harness.setLibrary(player1, List.of(matchingCard, nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Tolaria West");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
