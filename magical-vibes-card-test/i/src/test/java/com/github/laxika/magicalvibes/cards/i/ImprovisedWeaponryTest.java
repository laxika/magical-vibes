package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArmoryVeteran;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.z.ZarielArchdukeOfAvernus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImprovisedWeaponry.class, ArmoryVeteran.class, PowerWordKill.class, ZarielArchdukeOfAvernus.class})
class ImprovisedWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player and creates a Treasure token")
    void damagesPlayerAndCreatesTreasure() {
        int lifeBefore = gd.getLife(player2.getId());
        castImprovisedWeaponry(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature and creates a Treasure token")
    void damagesCreatureAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryVeteran());

        castImprovisedWeaponry(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Armory Veteran");
        harness.assertInGraveyard(player2, "Armory Veteran");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Can damage its controller and still creates a Treasure")
    void damagesControllerAndCreatesTreasure() {
        int lifeBefore = gd.getLife(player1.getId());

        castImprovisedWeaponry(player1.getId());

        harness.assertLife(player1, lifeBefore - 2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Improvised Weaponry");
    }

    @Test
    @DisplayName("Deals damage to a planeswalker by removing loyalty counters")
    void damagesPlaneswalkerAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ZarielArchdukeOfAvernus());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castImprovisedWeaponry(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Zariel, Archduke of Avernus");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when its only target becomes illegal")
    void illegalTargetPreventsTreasureCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoryVeteran());
        prepareImprovisedWeaponry();
        harness.castSorcery(player1, 0, target.getId());
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Armory Veteran");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Improvised Weaponry");
    }

    @Test
    @DisplayName("The Treasure can immediately be tapped and sacrificed for colored mana")
    void treasureCanImmediatelyProduceMana() {
        castImprovisedWeaponry(player2.getId());
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        assertThat(treasure.isTapped()).isFalse();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(manaBefore + 1);
    }

    private void castImprovisedWeaponry(UUID targetId) {
        prepareImprovisedWeaponry();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void prepareImprovisedWeaponry() {
        harness.setHand(player1, List.of(new ImprovisedWeaponry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
