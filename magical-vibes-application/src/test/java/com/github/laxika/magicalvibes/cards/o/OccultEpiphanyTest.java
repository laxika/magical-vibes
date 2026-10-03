package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OccultEpiphany.class, Forest.class, GrizzlyBears.class})
class OccultEpiphanyTest extends BaseCardTest {

    @Test
    void createsOneSpiritForEachDistinctDiscardedCardType() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new OccultEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void repeatedDiscardedCardTypesCreateOnlyOneSpirit() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new OccultEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(1);
    }
}
