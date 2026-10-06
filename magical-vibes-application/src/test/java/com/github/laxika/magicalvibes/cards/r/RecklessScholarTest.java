package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessScholar.class, Forest.class})
class RecklessScholarTest extends BaseCardTest {

    private Permanent readyScholar() {
        Permanent scholar = addCreatureReady(player1, new RecklessScholar());
        return scholar;
    }

    @Test
    @DisplayName("Target opponent draws a card, then discards a card")
    void opponentLoots() {
        Permanent scholar = readyScholar();
        harness.setHand(player2, List.of(new RecklessScholar()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(scholar.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Reckless Scholar");
    }

    @Test
    @DisplayName("Can target self to draw then discard")
    void controllerLoots() {
        readyScholar();
        harness.setHand(player1, List.of(new RecklessScholar()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        readyScholar();
        Permanent bear = addCreatureReady(player2, new RecklessScholar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetCanDiscardTheDrawnCard() {
        readyScholar();
        RecklessScholar original = new RecklessScholar();
        Forest drawn = new Forest();
        Forest controllerHand = new Forest();
        Forest controllerLibrary = new Forest();
        harness.setHand(player2, List.of(original));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player1, List.of(controllerHand));
        harness.setLibrary(player1, List.of(controllerLibrary));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyHandMustDiscardTheCardJustDrawn() {
        readyScholar();
        Forest drawn = new Forest();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawn));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emptyLibraryStillDiscardsBeforeTargetLoses() {
        readyScholar();
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent scholar = readyScholar();
        scholar.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new RecklessScholar());
        scholar.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
