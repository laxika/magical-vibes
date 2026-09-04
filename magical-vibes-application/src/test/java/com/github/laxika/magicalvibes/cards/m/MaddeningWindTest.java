package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaddeningWind.class, PaleBears.class, ZuranOrb.class})
class MaddeningWindTest extends BaseCardTest {

    private Permanent attachToOpponentCreature() {
        return attachToCreature(player2);
    }

    @Test
    @DisplayName("Enchanted creature's controller takes 2 damage at their upkeep")
    void enchantedControllerTakesTwoDamage() {
        attachToOpponentCreature();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Aura's controller takes no damage during their own upkeep")
    void auraControllerTakesNoDamage() {
        attachToOpponentCreature();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline cumulative upkeep

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Both upkeep abilities trigger when the Aura controller controls the enchanted creature")
    void bothUpkeepAbilitiesTriggerForSameController() {
        attachToCreature(player1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Maddening Wind");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Maddening Wind on the battlefield")
    void payingCumulativeUpkeepKeepsAura() {
        attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Maddening Wind");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one green mana per age counter")
    void cumulativeUpkeepCostsOneManaPerAgeCounter() {
        attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Maddening Wind");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(2);

        int manaBeforePayment = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(manaBeforePayment);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Maddening Wind")
    void decliningCumulativeUpkeepSacrificesAura() {
        attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Maddening Wind");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Maddening Wind");
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new PaleBears());
        harness.addToBattlefield(player2, new ZuranOrb());
        UUID artifactId = harness.getPermanentId(player2, "Zuran Orb");

        harness.setHand(player1, List.of(new MaddeningWind()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent attachToCreature(Player creatureController) {
        Permanent creature = addCreatureReady(creatureController, new PaleBears());

        harness.setHand(player1, List.of(new MaddeningWind()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }
}
