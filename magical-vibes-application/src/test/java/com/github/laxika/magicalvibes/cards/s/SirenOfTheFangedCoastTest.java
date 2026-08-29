package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SirenOfTheFangedCoastTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Siren enters with three +1/+1 counters")
    void opponentPaysTribute() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSiren();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "Siren of the Fanged Coast");
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute gains permanent control of a target creature")
    void opponentDeclinesTributeAndSirenGainsControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSiren();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanent(player1, "Siren of the Fanged Coast")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSiren() {
        harness.setHand(player1, List.of(new SirenOfTheFangedCoast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
