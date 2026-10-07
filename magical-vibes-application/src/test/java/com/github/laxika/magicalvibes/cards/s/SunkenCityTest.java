package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunkenCity.class, MerfolkOfThePearlTrident.class, MonssGoblinRaiders.class, Opalescence.class})
class SunkenCityTest extends BaseCardTest {

    @Test
    @DisplayName("Own blue creatures get +1/+1")
    void buffsOwnBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's blue creatures also get +1/+1")
    void buffsOpponentBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblue creatures are unaffected")
    void doesNotBuffNonblueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MonssGoblinRaiders());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to pay {U}{U} sacrifices Sunken City")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Sunken City");
        harness.assertInGraveyard(player1, "Sunken City");
    }

    @Test
    @DisplayName("Paying {U}{U} keeps Sunken City on the battlefield")
    void payingKeeps() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sunken City");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Accepting without enough blue mana still sacrifices Sunken City")
    void insufficientPaymentSacrifices() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sunken City");
        harness.assertInGraveyard(player1, "Sunken City");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunken City");
    }

    @Test
    @DisplayName("Sunken City boosts itself when Opalescence makes it a blue creature")
    void buffsItselfWhenAnimated() {
        Permanent city = harness.addToBattlefieldAndReturn(player1, new SunkenCity());
        harness.addToBattlefield(player2, new Opalescence());

        assertThat(gqs.getEffectivePower(gd, city)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, city)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Sunken Cities stack their bonuses on both players' blue creatures")
    void multipleCitiesStack() {
        harness.addToBattlefield(player1, new SunkenCity());
        harness.addToBattlefield(player2, new SunkenCity());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Sunken City removes its bonus from both players' blue creatures")
    void sacrificingRemovesBonuses() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Sunken City");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonblue mana cannot pay the upkeep and an unsuccessful payment spends no mana")
    void wrongColorPaymentSacrificesWithoutSpendingMana() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sunken City");
        harness.assertInGraveyard(player1, "Sunken City");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
