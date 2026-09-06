package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WriteIntoBeingTest extends BaseCardTest {

    @Test
    void manifestsChosenCardAndPutsTheOtherOnTop() {
        Card manifestedCard = new GrizzlyBears();
        Card topCard = new Forest();
        prepareSpell(List.of(manifestedCard, topCard));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void manifestsChosenCardAndPutsTheOtherOnBottom() {
        Card bottomCard = new Forest();
        Card manifestedCard = new GrizzlyBears();
        Card nextCard = new Forest();
        prepareSpell(List.of(bottomCard, manifestedCard, nextCard));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(nextCard);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(bottomCard);
    }

    private void prepareSpell(List<Card> library) {
        harness.setHand(player1, List.of(new WriteIntoBeing()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
