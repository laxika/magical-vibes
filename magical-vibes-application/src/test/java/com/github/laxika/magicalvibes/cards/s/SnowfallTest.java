package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Piracy;
import com.github.laxika.magicalvibes.cards.q.QuicksilverFountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Snowfall.class, Island.class, SnowCoveredIsland.class, Mountain.class, BalduvianBears.class,
        QuicksilverFountain.class, Piracy.class})
class SnowfallTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an Island adds one cumulative-upkeep-only blue")
    void islandAddsRestrictedBlue() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a snow Island adds two cumulative-upkeep-only blue")
    void snowIslandAddsTwoRestrictedBlue() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new SnowCoveredIsland());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Island land does not trigger Snowfall")
    void nonIslandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Effect is symmetric — opponent's Island also gets the restricted mana")
    void opponentIslandAlsoBenefits() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player2, new Island());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("A foreign Island gives Snowfall's restricted mana to that Island's controller")
    void foreignIslandBenefitsItsController() {
        harness.addToBattlefield(player1, new Snowfall());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Piracy()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.tapForeignLandForMana(player1, island.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A land that currently has the Island subtype triggers Snowfall")
    void effectiveIslandSubtypeTriggersSnowfall() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new QuicksilverFountain());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setCounterCount(CounterType.FLOOD, 1);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay a normal spell")
    void cannotPayNormalSpell() {
        harness.addToBattlefield(player1, new Snowfall());
        harness.addToBattlefield(player1, new Island());
        harness.tapPermanent(player1, 1);

        // Drain Island's unrestricted blue — leave only CU-restricted mana.
        var pool = gd.playerManaPools.get(player1.getId());
        while (pool.get(ManaColor.BLUE) > 0) {
            pool.remove(ManaColor.BLUE);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new BalduvianBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Paying cumulative upkeep with Snowfall mana keeps Snowfall")
    void paysCumulativeUpkeepWithSnowfallMana() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(snowfall.getCounterCount(CounterType.AGE)).isEqualTo(1);

        // Mana empties between steps — inject CU-only blue as Snowfall's Island trigger would.
        gd.playerManaPools.get(player1.getId()).addCumulativeUpkeepOnlyColored(ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowfall);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Mana from an Island can pay Snowfall's cumulative upkeep")
    void islandManaPaysCumulativeUpkeep() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.tapPermanent(player1, 1);

        var pool = gd.playerManaPools.get(player1.getId());
        while (pool.get(ManaColor.BLUE) > 0) {
            pool.remove(ManaColor.BLUE);
        }

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(snowfall.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowfall);
        assertThat(pool.getCumulativeUpkeepOnlyColored(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep increases with each age counter")
    void paysIncreasingCumulativeUpkeep() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerManaPools.get(player1.getId()).addCumulativeUpkeepOnlyColored(ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(snowfall.getCounterCount(CounterType.AGE)).isEqualTo(2);
        gd.playerManaPools.get(player1.getId()).addCumulativeUpkeepOnlyColored(ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowfall);
        assertThat(gd.playerManaPools.get(player1.getId()).getCumulativeUpkeepOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Snowfall")
    void declineSacrifices() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowfall);
        harness.assertInGraveyard(player1, "Snowfall");
    }

    @Test
    @DisplayName("Snowfall's cumulative upkeep does not trigger during an opponent's upkeep")
    void cumulativeUpkeepDoesNotTriggerDuringOpponentsUpkeep() {
        Permanent snowfall = harness.addToBattlefieldAndReturn(player1, new Snowfall());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(snowfall.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowfall);
    }
}
