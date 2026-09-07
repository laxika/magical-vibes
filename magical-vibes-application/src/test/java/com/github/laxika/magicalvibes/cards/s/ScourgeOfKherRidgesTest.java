package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeOfKherRidges.class, GrizzlyBears.class, SuntailHawk.class})
class ScourgeOfKherRidgesTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability damages creatures without flying")
    void firstAbilityDamagesOnlyNonFlyers() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        Permanent groundCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingCreature = addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The second ability damages other creatures with flying")
    void secondAbilityDamagesOnlyOtherFlyers() {
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player2, new SuntailHawk());
        Permanent groundCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scourge of Kher Ridges");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(scourge.getMarkedDamage()).isZero();
        assertThat(groundCreature.getMarkedDamage()).isZero();
    }
}
