package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AegisOfTheGods.class, BeaconOfImmortality.class, Shock.class, Humble.class})
class AegisOfTheGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target the controller with a spell")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can still target themselves")
    void controllerCanTargetSelf() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 40);
    }

    @Test
    @DisplayName("Aegis of the Gods itself can still be targeted")
    void permanentItselfCanBeTargeted() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        var aegisId = harness.getPermanentId(player1, "Aegis of the Gods");
        harness.castInstant(player2, 0, aegisId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aegis of the Gods");
    }

    @Test
    @DisplayName("The opponent does not gain hexproof from Aegis")
    void opponentCanStillBeTargeted() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Hexproof ends as soon as Aegis leaves the battlefield")
    void controllerCanBeTargetedAfterAegisDies() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Aegis of the Gods"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Aegis of the Gods");

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Losing all abilities removes the controller's hexproof")
    void controllerCanBeTargetedWhenAegisLosesAbilities() {
        harness.addToBattlefield(player1, new AegisOfTheGods());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Humble(), new Shock()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Aegis of the Gods"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aegis of the Gods");

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }
}
