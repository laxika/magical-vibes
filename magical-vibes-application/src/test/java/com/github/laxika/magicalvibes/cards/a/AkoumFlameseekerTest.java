package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.Wastes;
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

@CardUsed({AkoumFlameseeker.class, Wastes.class})
class AkoumFlameseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally, discards a card, and draws a card")
    void cohortDiscardsAndDraws() {
        Wastes discardedCard = new Wastes();
        Wastes drawnCard = new Wastes();
        harness.setHand(player1, new ArrayList<>(List.of(discardedCard)));
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new AkoumFlameseeker());

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
        Wastes topCard = new Wastes();
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, List.of(topCard));

        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new AkoumFlameseeker());

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

    @Test
    @DisplayName("The second Ally may have summoning sickness and tapping is paid before resolution")
    void canTapSummoningSickAllyAsCost() {
        Wastes discardedCard = new Wastes();
        Wastes drawnCard = new Wastes();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new AkoumFlameseeker());
        ally.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null);

        assertThat(flameseeker.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
    }

    @Test
    @DisplayName("A summoning sick Flameseeker cannot activate cohort")
    void summoningSickSourceCannotActivate() {
        Permanent flameseeker = harness.addToBattlefieldAndReturn(player1, new AkoumFlameseeker());
        flameseeker.setSummoningSick(true);
        Permanent ally = addCreatureReady(player1, new AkoumFlameseeker());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flameseeker.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped Ally cannot pay the cohort cost")
    void tappedAllyCannotPayCost() {
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new AkoumFlameseeker());
        ally.tap();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(flameseeker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's untapped Ally cannot pay the cohort cost")
    void opponentsAllyCannotPayCost() {
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player2, new AkoumFlameseeker());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(flameseeker.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Discard is mandatory at resolution and the controller chooses which card")
    void controllerChoosesDiscardFromMultipleCards() {
        Wastes keptCard = new Wastes();
        Wastes discardedCard = new Wastes();
        Wastes drawnCard = new Wastes();
        harness.setHand(player1, List.of(keptCard, discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        addCreatureReady(player1, new AkoumFlameseeker());

        harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
    }

    @Test
    @DisplayName("A tapped Flameseeker cannot activate even with an untapped Ally")
    void tappedSourceCannotActivate() {
        Permanent flameseeker = addCreatureReady(player1, new AkoumFlameseeker());
        Permanent ally = addCreatureReady(player1, new AkoumFlameseeker());
        flameseeker.tap();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(flameseeker), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ally.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
