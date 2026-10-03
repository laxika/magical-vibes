package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiselaTheBrokenBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrunaTheFadingLight.class, GiselaTheBrokenBlade.class,
        GrizzlyBears.class, YouthfulKnight.class})
class BrunaTheFadingLightTest extends BaseCardTest {

    @Test
    void castReturnsHumanCreature() {
        YouthfulKnight human = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(human));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertNotInGraveyard(player1, "Youthful Knight");
    }

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
        resolveAllTriggers();

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
        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    @Test
    void castRequiresTargetEvenWhenReturnMayBeDeclined() {
        harness.setGraveyard(player1, List.of(new YouthfulKnight()));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringWithoutCastingDoesNotReturnCreature() {
        harness.setGraveyard(player1, List.of(new YouthfulKnight()));

        harness.enterBattlefieldAndReturn(player1, new BrunaTheFadingLight());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    @Test
    void castCannotReturnCreatureFromOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GiselaTheBrokenBlade()));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertNotOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertInGraveyard(player2, "Gisela, the Broken Blade");
    }

    @Test
    void attackingDoesNotTapBruna() {
        var bruna = addCreatureReady(player1, new BrunaTheFadingLight());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(bruna.isTapped()).isFalse();
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Casting with no Angel or Human creature card only resolves Bruna")
    void castWithNoMatchingCard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BrunaTheFadingLight()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
