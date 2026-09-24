package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlacialRevelation.class, Forest.class, GrizzlyBears.class, Shock.class})
class GlacialRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any chosen snow permanents into hand and the rest into the graveyard")
    void choosesSnowPermanentsAndMillsTheRest() {
        Card snowForest = snow(new Forest());
        Card snowBears = snow(new GrizzlyBears());
        Card snowShock = snow(new Shock());
        Card nonsnowForest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(snowForest, snowBears, snowShock, nonsnowForest, bears, shock));

        castGlacialRevelation();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(snowForest.getId(), snowBears.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(snowForest.getId(), snowBears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(snowForest, snowBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(snowShock, nonsnowForest, bears, shock);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May decline all snow permanents")
    void mayDeclineAllSnowPermanents() {
        Card snowForest = snow(new Forest());
        Card snowBears = snow(new GrizzlyBears());
        harness.setLibrary(player1, List.of(snowForest, snowBears));

        castGlacialRevelation();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snowForest, snowBears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castGlacialRevelation() {
        harness.setHand(player1, List.of(new GlacialRevelation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private Card snow(Card card) {
        card.setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return card;
    }
}
