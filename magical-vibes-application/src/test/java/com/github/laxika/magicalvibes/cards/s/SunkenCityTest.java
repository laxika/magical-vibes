package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Drowned;
import com.github.laxika.magicalvibes.cards.m.MarshGoblins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunkenCity.class, Drowned.class, MarshGoblins.class})
class SunkenCityTest extends BaseCardTest {

    @Test
    @DisplayName("Own blue creatures get +1/+1")
    void buffsOwnBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent drowned = harness.addToBattlefieldAndReturn(player1, new Drowned());

        assertThat(gqs.getEffectivePower(gd, drowned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drowned)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's blue creatures also get +1/+1")
    void buffsOpponentBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent drowned = harness.addToBattlefieldAndReturn(player2, new Drowned());

        assertThat(gqs.getEffectivePower(gd, drowned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drowned)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblue creatures are unaffected")
    void doesNotBuffNonblueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        Permanent marshGoblins = harness.addToBattlefieldAndReturn(player1, new MarshGoblins());

        assertThat(gqs.getEffectivePower(gd, marshGoblins)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, marshGoblins)).isEqualTo(1);
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
}
