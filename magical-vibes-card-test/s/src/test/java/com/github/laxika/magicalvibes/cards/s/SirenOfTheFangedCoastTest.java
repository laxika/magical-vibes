package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SirenOfTheFangedCoast.class, NyxbornTriton.class, BileBlight.class})
class SirenOfTheFangedCoastTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Siren enters with three +1/+1 counters")
    void opponentPaysTribute() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        castSiren();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "Siren of the Fanged Coast");
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player2, "Nyxborn Triton")).hasSize(1);
        assertThat(findPermanents(player1, "Nyxborn Triton")).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute gains permanent control of a target creature")
    void opponentDeclinesTributeAndSirenGainsControl() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        castSiren();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Nyxborn Triton"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Nyxborn Triton")).hasSize(1);
        assertThat(findPermanents(player2, "Nyxborn Triton")).isEmpty();
        assertThat(findPermanent(player1, "Siren of the Fanged Coast")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With no other creatures, declining tribute can target the Siren itself")
    void canTargetItself() {
        castSiren();

        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Siren of the Fanged Coast"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siren of the Fanged Coast")).hasSize(1);
        assertThat(findPermanent(player1, "Siren of the Fanged Coast")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The control trigger still resolves after the Siren dies")
    void controlTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        castSiren();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Nyxborn Triton"));

        castBileBlightAt("Siren of the Fanged Coast", player1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Siren of the Fanged Coast");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        harness.assertNotOnBattlefield(player2, "Nyxborn Triton");
    }

    @Test
    @DisplayName("Control does not revert when the Siren leaves after its trigger resolves")
    void controlPersistsAfterSourceRemoval() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        castSiren();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Nyxborn Triton"));
        harness.passBothPriorities();

        castBileBlightAt("Siren of the Fanged Coast", player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Siren of the Fanged Coast");
        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        harness.assertNotOnBattlefield(player2, "Nyxborn Triton");
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        harness.assertNotOnBattlefield(player2, "Nyxborn Triton");
    }

    @Test
    @DisplayName("The control trigger has no effect when its target dies in response")
    void targetRemovedInResponse() {
        harness.addToBattlefield(player2, new NyxbornTriton());
        castSiren();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Nyxborn Triton"));

        castBileBlightAt("Nyxborn Triton", player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nyxborn Triton");
        harness.assertNotOnBattlefield(player1, "Nyxborn Triton");
        harness.assertOnBattlefield(player1, "Siren of the Fanged Coast");
        assertThat(gd.stack).isEmpty();
    }

    private void castBileBlightAt(String cardName, Player controller) {
        harness.setHand(player2, List.of(new BileBlight()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(controller, cardName));
    }

    private void castSiren() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SirenOfTheFangedCoast(), "{3}{U}{U}");
        harness.passBothPriorities();
    }
}
