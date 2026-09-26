package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjinnOfInfiniteDeceits.class, GrizzlyBears.class, HillGiant.class, KrenkoMobBoss.class})
class DjinnOfInfiniteDeceitsTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of two target nonlegendary creatures")
    void exchangesControlOfNonlegendaryCreatures() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Rejects a legendary creature as a target")
    void rejectsLegendaryCreatureTarget() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new KrenkoMobBoss());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(own.getId(), legendary.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the exchange ability during combat")
    void cannotActivateDuringCombat() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You can't activate this ability during combat.");
    }
}
