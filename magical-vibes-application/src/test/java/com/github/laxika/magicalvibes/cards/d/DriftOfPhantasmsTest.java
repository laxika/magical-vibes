package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DriftOfPhantasms.class, Convolute.class, DizzySpell.class, GrizzlyBears.class})
class DriftOfPhantasmsTest extends BaseCardTest {

    @Test
    void transmuteSearchesForTheSameManaValue() {
        Convolute matchingCard = new Convolute();
        DizzySpell lowerManaValue = new DizzySpell();
        GrizzlyBears lowerManaValueToo = new GrizzlyBears();
        harness.setHand(player1, List.of(new DriftOfPhantasms()));
        harness.setLibrary(player1, List.of(matchingCard, lowerManaValue, lowerManaValueToo));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Drift of Phantasms");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
