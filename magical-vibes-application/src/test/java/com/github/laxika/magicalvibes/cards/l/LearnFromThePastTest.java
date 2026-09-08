package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LearnFromThePast.class, GiantSpider.class, GrizzlyBears.class})
class LearnFromThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles the target graveyard and draws a card")
    void shufflesTargetGraveyardAndDrawsCard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LearnFromThePast()));
        harness.setLibrary(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        int targetLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetLibrarySize + 2);
        harness.assertInHand(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LearnFromThePast()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
