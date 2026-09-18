package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SadisticHypnotist.class, AngelicWall.class, Forest.class})
class SadisticHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes the target player discard two cards")
    void sacrificesCreatureAndDiscardsTwoCards() {
        addReadyHypnotist(player1);
        harness.addToBattlefield(player1, new AngelicWall());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest()));
        readyForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Angelic Wall"));

        harness.assertInGraveyard(player1, "Angelic Wall");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        addReadyHypnotist(player1);
        harness.addToBattlefield(player1, new AngelicWall());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        readyForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Angelic Wall"));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadyHypnotist(player1);
        harness.addToBattlefield(player1, new AngelicWall());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("May sacrifice Sadistic Hypnotist itself")
    void maySacrificeItself() {
        addReadyHypnotist(player1);
        harness.addToBattlefield(player1, new AngelicWall());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        readyForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sadistic Hypnotist"));

        harness.assertNotOnBattlefield(player1, "Sadistic Hypnotist");
        harness.assertInGraveyard(player1, "Sadistic Hypnotist");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discards the only card when the target has fewer than two cards")
    void discardsOnlyAvailableCard() {
        addReadyHypnotist(player1);
        harness.addToBattlefield(player1, new AngelicWall());
        harness.setHand(player2, List.of(new Forest()));
        readyForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Angelic Wall"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private void addReadyHypnotist(Player player) {
        addCreatureReady(player, new SadisticHypnotist());
    }

    private void readyForSorcerySpeed(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
