package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CatOwl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterbendingLesson.class, CatOwl.class})
class WaterbendingLessonTest extends BaseCardTest {

    @Test
    void drawsThreeCardsThenDiscardsWithoutWaterbend() {
        harness.setHand(player1, List.of(new WaterbendingLesson(), new CatOwl()));
        harness.setLibrary(player1, List.of(new CatOwl(), new CatOwl(), new CatOwl()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void offersWaterbendAfterDrawingInsteadOfRequiringPaymentDuringCasting() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        harness.setLibrary(player1, List.of(new CatOwl(), new CatOwl(), new CatOwl()));

        harness.castFromHand(player1, new WaterbendingLesson(), "{3}{U}");

        assertThat(firstSource.isTapped()).isFalse();
        assertThat(secondSource.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(firstSource.isTapped()).isFalse();
        assertThat(secondSource.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNull();
    }

    @Test
    void offersWaterbendUsingManaAvailableWhenTheSpellResolves() {
        harness.setLibrary(player1, List.of(new CatOwl(), new CatOwl(), new CatOwl()));
        harness.castFromHand(player1, new WaterbendingLesson(), "{3}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
