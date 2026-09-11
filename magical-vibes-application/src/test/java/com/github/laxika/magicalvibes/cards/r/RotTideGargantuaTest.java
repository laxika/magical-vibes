package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotTideGargantua.class, GrizzlyBears.class})
class RotTideGargantuaTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Rot-Tide Gargantua and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Rot-Tide Gargantua");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("Exploiting a creature makes each opponent sacrifice a creature")
    void exploitMakesEachOpponentSacrifice() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRotTideGargantua();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, sacrificed.getId());

        harness.assertOnBattlefield(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(remaining);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(sacrificed);
    }

    @Test
    @DisplayName("Exploiting Rot-Tide Gargantua itself still makes each opponent sacrifice")
    void exploitingItselfStillTriggers() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRotTideGargantua();
        Permanent gargantua = findPermanent(player1, "Rot-Tide Gargantua");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gargantua.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rot-Tide Gargantua");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castRotTideGargantua() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RotTideGargantua()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
