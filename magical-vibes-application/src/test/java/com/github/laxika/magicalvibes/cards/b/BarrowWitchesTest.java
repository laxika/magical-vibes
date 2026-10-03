package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowWitches.class, YouthfulKnight.class})
class BarrowWitchesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target Knight card from the controller's graveyard to hand")
    void returnsTargetKnightFromGraveyardToHand() {
        YouthfulKnight knight = new YouthfulKnight();
        BarrowWitches nonKnight = new BarrowWitches();
        harness.setGraveyard(player1, List.of(knight, nonKnight));

        castBarrowWitches();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(knight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(knight.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Youthful Knight");
        harness.assertInGraveyard(player1, "Barrow Witches");
    }

    @Test
    @DisplayName("ETB does not target a non-Knight card")
    void doesNotTargetNonKnight() {
        harness.setGraveyard(player1, List.of(new BarrowWitches()));

        castBarrowWitches();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Barrow Witches");
    }

    @Test
    @DisplayName("ETB only offers Knights in its controller's graveyard")
    void excludesOpponentsKnights() {
        YouthfulKnight ownKnight = new YouthfulKnight();
        YouthfulKnight opposingKnight = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(ownKnight));
        harness.setGraveyard(player2, List.of(opposingKnight));

        castBarrowWitches();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownKnight.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownKnight.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("ETB returns only the selected Knight when multiple Knights are eligible")
    void returnsOnlySelectedKnight() {
        YouthfulKnight first = new YouthfulKnight();
        YouthfulKnight second = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(first, second));

        castBarrowWitches();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("ETB does nothing when only the opponent has a Knight in their graveyard")
    void noTargetInControllersGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new YouthfulKnight()));

        castBarrowWitches();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Barrow Witches");
        harness.assertNotInHand(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when the targeted Knight leaves the graveyard")
    void targetLeavingGraveyardDoesNotReturnAnotherKnight() {
        YouthfulKnight target = new YouthfulKnight();
        YouthfulKnight other = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(target, other));

        castBarrowWitches();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Youthful Knight");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).containsExactly(target);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBarrowWitches() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BarrowWitches(), "{4}{B}");
        harness.passBothPriorities();
    }
}
