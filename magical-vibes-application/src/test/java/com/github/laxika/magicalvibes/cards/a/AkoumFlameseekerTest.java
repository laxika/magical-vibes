package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkoumFlameseeker.class, HadaFreeblade.class, Forest.class})
class AkoumFlameseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally, discards a card, and draws a card")
    void cohortDiscardsAndDraws() {
        Forest discardedCard = new Forest();
        Forest drawnCard = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discardedCard)));
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(flameseeker.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("An empty hand does not draw a card")
    void emptyHandDoesNotDraw() {
        Forest topCard = new Forest();
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(topCard));

        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null);
        harness.passBothPriorities();

        assertThat(flameseeker.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(flameseeker.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
