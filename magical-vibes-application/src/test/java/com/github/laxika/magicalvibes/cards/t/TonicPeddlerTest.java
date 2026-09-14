package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TonicPeddler.class, GrizzlyBears.class})
class TonicPeddlerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and paying white mana makes target player gain 3 life")
    void gainsLifeForTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife + 3);
        assertThat(peddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target its controller")
    void gainsLifeForController() {
        Permanent peddler = addCreatureReady(player1, new TonicPeddler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, initialLife + 3);
        assertThat(peddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new TonicPeddler());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
