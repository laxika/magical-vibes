package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.o.OrbsOfWarding;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EladamrisVineyard.class, Disenchant.class, OrbsOfWarding.class})
class EladamrisVineyardTest extends BaseCardTest {

    @Test
    @DisplayName("The controller adds {G}{G} at the beginning of their first main phase")
    void addsTwoGreenOnControllersFirstMain() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The opponent also adds {G}{G} on their own first main phase")
    void addsTwoGreenOnOpponentsFirstMain() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The mana trigger waits on the stack before adding mana")
    void triggerResolvesFromTheStack() {
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Vineyard adds {G}{G} independently")
    void eachVineyardAddsTwoGreen() {
        harness.addToBattlefield(player1, new EladamrisVineyard());
        harness.addToBattlefield(player1, new EladamrisVineyard());

        advanceToPrecombatMain(player1);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("The opponent adds mana even when they have hexproof")
    void addsManaToOpponentWithHexproof() {
        harness.addToBattlefield(player1, new EladamrisVineyard());
        harness.addToBattlefield(player2, new OrbsOfWarding());

        advanceToPrecombatMain(player2);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Vineyards controlled by different players both give mana to the active player")
    void vineyardsWithDifferentControllersAwardManaToSamePlayer() {
        harness.addToBattlefield(player1, new EladamrisVineyard());
        harness.addToBattlefield(player2, new EladamrisVineyard());

        advanceToPrecombatMain(player2);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("No mana is added at the beginning of the postcombat main phase")
    void doesNotTriggerOnPostcombatMain() {
        harness.addToBattlefield(player1, new EladamrisVineyard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Casting Vineyard after the first main phase begins does not immediately add mana")
    void enteringDuringMainPhaseDoesNotTriggerRetroactively() {
        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of(new EladamrisVineyard()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eladamri's Vineyard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Removing Vineyard in response does not stop its mana trigger")
    void triggerResolvesAfterSourceIsDestroyed() {
        var vineyard = harness.addToBattlefieldAndReturn(player1, new EladamrisVineyard());
        harness.setHand(player2, List.of(new Disenchant()));

        advanceToPrecombatMain(player2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, vineyard.getId());

        harness.assertInGraveyard(player1, "Eladamri's Vineyard");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
