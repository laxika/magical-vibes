package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.cards.l.LeoninBola;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherVial.class, CrazedGoblin.class, DroolingOgre.class, LeoninBola.class})
class AetherVialTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a charge counter on Aether Vial")
    void upkeepAcceptedAddsChargeCounter() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves Aether Vial unchanged")
    void upkeepDeclinedDoesNotAddChargeCounter() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Puts a creature with matching mana value onto the battlefield")
    void putsCreatureWithMatchingManaValue() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        vial.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new DroolingOgre(), new CrazedGoblin()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Drooling Ogre");
        harness.assertInHand(player1, "Crazed Goblin");
        assertThat(vial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not offer a creature with a different mana value")
    void requiresMatchingManaValue() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        vial.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new CrazedGoblin()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        harness.assertInHand(player1, "Crazed Goblin");
    }

    @Test
    @DisplayName("May decline to put a matching creature onto the battlefield")
    void mayDeclineMatchingCreature() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        vial.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new DroolingOgre()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Drooling Ogre");
        harness.assertInHand(player1, "Drooling Ogre");
        assertThat(vial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not offer a noncreature card with matching mana value")
    void requiresCreatureCard() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        vial.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new LeoninBola()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Bola");
        harness.assertInHand(player1, "Leonin Bola");
    }
}
