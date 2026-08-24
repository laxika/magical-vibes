package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathPitFighter.class, GrizzlyBears.class, Island.class, Shock.class})
class FalkenrathPitFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate before an opponent loses life this turn")
    void cannotActivateBeforeOpponentLosesLife() {
        addReadyPitFighter();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    @DisplayName("After an opponent loses life, discarding and sacrificing a Vampire draws two cards")
    void drawsTwoCardsAfterPayingCosts() {
        addReadyPitFighter();
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new Shock(), new GrizzlyBears())));

        harness.addMana(player1, ManaColor.RED, 1);
        forceMainPhase(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        forceMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Falkenrath Pit Fighter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island", "Island");
    }

    private Permanent addReadyPitFighter() {
        Permanent permanent = new Permanent(new FalkenrathPitFighter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void forceMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
