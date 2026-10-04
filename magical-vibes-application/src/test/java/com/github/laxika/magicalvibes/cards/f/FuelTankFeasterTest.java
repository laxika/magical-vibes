package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimeStretch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuelTankFeaster.class, CrawWurm.class, GrizzlyBears.class, TimeStretch.class})
class FuelTankFeasterTest extends BaseCardTest {

    @Test
    void reducesTheGreatestManaValueCreatureCardInHand() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new GrizzlyBears(), new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 1);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Craw Wurm"));
    }

    @Test
    void tapsForOneManaOfAnyColor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player1, new FuelTankFeaster());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void reducesExactlyOneOfTwoTiedCreatureCards() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm(), new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 11);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void multipleFeastersGrantCumulativeReductions() {
        addCreatureReady(player1, new FuelTankFeaster());
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));

        advanceToPrecombatMain(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void selectsFromTheHandAtResolution() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void doesNothingWhenNoCreatureCardsRemainInHand() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringSecondMainPhase() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 5);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionsCannotPayColoredManaCosts() {
        addCreatureReady(player1, new FuelTankFeaster());
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void reductionPersistsAndAccumulatesOnLaterTurns() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void manaAbilityCannotBeActivatedWhileSummoningSick() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new FuelTankFeaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ignoresNoncreatureCardsWithGreaterManaValue() {
        addCreatureReady(player1, new FuelTankFeaster());
        harness.setHand(player1, List.of(new TimeStretch(), new CrawWurm()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 1);

        assertThat(gd.stack).hasSize(1);
        harness.assertInHand(player1, "Time Stretch");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void advanceToPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
