package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PewterGolem.class, PyriteSpellbomb.class, Shatter.class})
class PewterGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Pewter Golem's ability grants a regeneration shield and pays {1}{B}")
    void activatingAbilityGrantsRegenerationShield() {
        Permanent golem = addCreatureReady(player1, new PewterGolem());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Pewter Golem's regeneration shield clears during cleanup")
    void regenerationShieldClearsAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new PewterGolem());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(golem.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves Pewter Golem from lethal damage")
    void regenerationShieldPreventsLethalDamage() {
        Permanent golem = addCreatureReady(player1, new PewterGolem());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new PyriteSpellbomb());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isZero();
        assertThat(golem.getMarkedDamage()).isZero();
        assertThat(golem.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Pewter Golem");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Pewter Golem can activate its regeneration ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new PewterGolem());
        golem.setSummoningSick(true);
        golem.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(golem.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each regeneration activation saves Pewter Golem from one destruction attempt")
    void multipleShieldsProtectAgainstSeparateDestructionAttempts() {
        Permanent golem = addCreatureReady(player1, new PewterGolem());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(2);
        assertThat(golem.isTapped()).isFalse();
        harness.setHand(player2, List.of(new Shatter(), new Shatter(), new Shatter()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, golem.getId());

        harness.assertOnBattlefield(player1, "Pewter Golem");
        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(golem.isTapped()).isTrue();

        harness.castAndResolveInstant(player2, 0, golem.getId());

        harness.assertOnBattlefield(player1, "Pewter Golem");
        assertThat(golem.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, golem.getId());

        harness.assertNotOnBattlefield(player1, "Pewter Golem");
        harness.assertInGraveyard(player1, "Pewter Golem");
    }
}
