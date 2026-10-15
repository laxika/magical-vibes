package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AbsorbingManAndTitania;
import com.github.laxika.magicalvibes.cards.a.AIMBot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantManGargantuanGenius.class, AbsorbingManAndTitania.class, AIMBot.class})
class GiantManGargantuanGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one green mana for each controlled creature with power 4 or greater")
    void addsManaForControlledCreaturesWithPowerAtLeastFour() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());
        harness.addToBattlefield(player1, new AIMBot());

        advanceToPrecombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only creatures controlled by Giant-Man's controller")
    void countsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.addToBattlefield(player2, new AbsorbingManAndTitania());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Triggers during its controller's first main phase")
    void triggersDuringControllersFirstMainPhase() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Counts creatures that reach exactly four power before resolution")
    void countsCurrentPowerAtResolution() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        var bot = harness.addToBattlefieldAndReturn(player1, new AIMBot());

        advanceToPrecombatMain(player1);
        bot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds no mana when all controlled creatures have power below four")
    void addsNoManaWhenNoCreaturesQualify() {
        var giant = harness.addToBattlefieldAndReturn(player1, new GiantManGargantuanGenius());
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Resolves after Giant-Man leaves and counts only remaining creatures")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.addToBattlefield(player1, new AbsorbingManAndTitania());

        advanceToPrecombatMain(player1);
        var giant = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(giant.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger at the beginning of the second main phase")
    void doesNotTriggerDuringSecondMainPhase() {
        harness.addToBattlefield(player1, new GiantManGargantuanGenius());
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
