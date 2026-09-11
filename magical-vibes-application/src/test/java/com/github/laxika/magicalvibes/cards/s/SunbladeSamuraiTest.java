package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunbladeSamurai.class, Plains.class, Forest.class})
class SunbladeSamuraiTest extends BaseCardTest {

    @Test
    void channelSearchesForABasicPlainsAndGainsLife() {
        harness.setHand(player1, List.of(new SunbladeSamurai()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunblade Samurai");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Plains", "Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Plains");
    }
}
