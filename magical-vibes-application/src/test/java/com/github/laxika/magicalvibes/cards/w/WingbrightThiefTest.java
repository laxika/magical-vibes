package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingbrightThief.class, ChildOfNight.class, Forest.class})
class WingbrightThiefTest extends BaseCardTest {

    @Test
    void choosesNonlandCardAndItTriggersWhenCast() {
        Card land = new Forest();
        Card nonland = new ChildOfNight();
        harness.setHand(player2, new ArrayList<>(List.of(land, nonland)));
        harness.setHand(player1, List.of(new WingbrightThief()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PerpetualTargetCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualTargetCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLACK, 2);
        int controllerHandSizeBeforeCast = gd.playerHands.get(player1.getId()).size();
        harness.castCreature(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSizeBeforeCast + 1);
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new WingbrightThief()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
