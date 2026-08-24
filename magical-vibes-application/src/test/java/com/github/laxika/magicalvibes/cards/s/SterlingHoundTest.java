package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SterlingHound.class, GrizzlyBears.class})
class SterlingHoundTest extends BaseCardTest {

    @Test
    void entersWithSurveilTwo() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SterlingHound()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    void surveilCanPutOneCardIntoTheGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SterlingHound()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }
}
