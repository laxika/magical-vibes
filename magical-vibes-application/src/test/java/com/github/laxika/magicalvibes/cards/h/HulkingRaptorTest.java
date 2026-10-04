package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulkingRaptor.class, Shock.class})
class HulkingRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("The controller adds {G}{G} at the beginning of their first main phase")
    void addsTwoGreenOnControllersFirstMain() {
        harness.addToBattlefield(player1, new HulkingRaptor());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The trigger does not happen during an opponent's first main phase")
    void doesNotAddManaOnOpponentsFirstMain() {
        harness.addToBattlefield(player1, new HulkingRaptor());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        var raptor = harness.addToBattlefieldAndReturn(player1, new HulkingRaptor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, raptor.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Hulking Raptor");
    }

    @Test
    void manaTriggerUsesTheStack() {
        harness.addToBattlefield(player1, new HulkingRaptor());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerInSecondMainPhase() {
        harness.addToBattlefield(player1, new HulkingRaptor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        var raptor = harness.addToBattlefieldAndReturn(player1, new HulkingRaptor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, raptor.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(raptor.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        harness.assertOnBattlefield(player1, "Hulking Raptor");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        var raptor = harness.addToBattlefieldAndReturn(player1, new HulkingRaptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, raptor.getId());

        assertThat(raptor.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hulking Raptor");
        harness.assertInGraveyard(player1, "Shock");
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
