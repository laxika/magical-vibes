package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfIth.class, Forest.class, GrizzlyBears.class})
class WandOfIthTest extends BaseCardTest {

    private void readyWand() {
        harness.addToBattlefield(player1, new WandOfIth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateOn(Card card) {
        harness.setHand(player2, List.of(card));
        readyWand();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player may pay 1 life to keep a revealed land")
    void paysOneLifeToKeepLand() {
        activateOn(new Forest());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining discards a revealed nonland card")
    void declineDiscardsNonland() {
        activateOn(new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A player who cannot pay the mana-value life cost discards automatically")
    void cannotPayDiscardsAutomatically() {
        harness.setLife(player2, 1);
        activateOn(new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Can target only a player")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new Forest());
        readyWand();
        var targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's turn")
    void cannotActivateOnOpponentTurn() {
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new WandOfIth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
