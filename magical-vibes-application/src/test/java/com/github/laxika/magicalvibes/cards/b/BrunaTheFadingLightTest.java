package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiselaTheBrokenBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrunaTheFadingLightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting returns a targeted Angel or Human creature card to the battlefield")
    void castReturnsTargetedAngelOrHuman() {
        GiselaTheBrokenBlade angel = new GiselaTheBrokenBlade();
        YouthfulKnight human = new YouthfulKnight();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(angel, human, bears));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(angel.getId(), human.getId());

        harness.handleMultipleCardsChosen(player1, List.of(angel.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting can decline the optional graveyard return")
    void castCanDeclineReturn() {
        YouthfulKnight human = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(human));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("Casting with no Angel or Human creature card only resolves Bruna")
    void castWithNoMatchingCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
