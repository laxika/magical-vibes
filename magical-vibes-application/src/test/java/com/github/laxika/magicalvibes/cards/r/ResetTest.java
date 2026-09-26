package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.h.Hammerheim;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reset.class, Karakas.class, Hammerheim.class, AmrouKithkin.class})
class ResetTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all lands controlled by its caster")
    void untapsControlledLandsOnly() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Karakas());
        land.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AmrouKithkin());
        creature.tap();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Karakas());
        opponentLand.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Reset(), "{U}{U}");
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps every land controlled by its caster")
    void untapsEveryControlledLand() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Karakas());
        firstLand.tap();
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Hammerheim());
        secondLand.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.castFromHand(player1, new Reset(), "{U}{U}");
        harness.passBothPriorities();

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can be cast during an opponent's draw step after upkeep")
    void canBeCastAfterOpponentsUpkeep() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Karakas());
        land.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.castFromHand(player1, new Reset(), "{U}{U}");
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast during an opponent's upkeep")
    void cannotBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromHand(player1, new Reset(), "{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast during its controller's turn")
    void cannotBeCastDuringControllersTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromHand(player1, new Reset(), "{U}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
