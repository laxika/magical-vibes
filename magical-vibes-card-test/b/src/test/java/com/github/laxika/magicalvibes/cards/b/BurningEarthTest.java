package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningEarth.class, Forest.class, Mountain.class, FaerieConclave.class, Mutavault.class, Naturalize.class})
class BurningEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Burning Earth puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BurningEarth()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Burning Earth");
    }

    @Test
    @DisplayName("Opponent tapping a nonbasic land for mana takes 1 damage")
    void opponentTappingNonbasicLandTakesDamage() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player2, new FaerieConclave());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Controller tapping a nonbasic land for mana also takes 1 damage")
    void controllerTappingNonbasicLandTakesDamage() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player1, new FaerieConclave());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping a basic land for mana deals no damage")
    void tappingBasicLandDealsNoDamage() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each nonbasic land tap triggers Burning Earth separately")
    void eachNonbasicTapTriggers() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player2, new FaerieConclave());
        harness.addToBattlefield(player2, new FaerieConclave());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Mana is available before Burning Earth's damage resolves")
    void manaIsAvailableBeforeDamageResolves() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player2, new Mutavault());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each Burning Earth triggers for the same nonbasic land tap")
    void multipleCopiesEachDealDamage() {
        harness.addToBattlefield(player1, new BurningEarth());
        harness.addToBattlefield(player2, new BurningEarth());
        harness.addToBattlefield(player2, new Mutavault());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing Burning Earth does not stop its pending damage trigger")
    void pendingTriggerSurvivesSourceRemoval() {
        var burningEarth = harness.addToBattlefieldAndReturn(player1, new BurningEarth());
        harness.addToBattlefield(player1, new Mutavault());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burningEarth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burning Earth");
        harness.assertNotOnBattlefield(player1, "Burning Earth");
        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }
}
