package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AkkiRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MannichiTheFeveredDream.class, AkkiRaider.class})
class MannichiTheFeveredDreamTest extends BaseCardTest {

    @Test
    @DisplayName("Switches the power and toughness of each creature")
    void switchesEachCreature() {
        Permanent mannichi = harness.addToBattlefieldAndReturn(player1, new MannichiTheFeveredDream());
        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mannichi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mannichi)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only affects creatures on the battlefield as the ability resolves")
    void doesNotAffectCreaturesThatEnterLater() {
        Permanent mannichi = harness.addToBattlefieldAndReturn(player1, new MannichiTheFeveredDream());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());

        assertThat(gqs.getEffectivePower(gd, mannichi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mannichi)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new MannichiTheFeveredDream());
        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two activations restore each creature's original power and toughness")
    void twoSwitchesCancel() {
        Permanent mannichi = harness.addToBattlefieldAndReturn(player1, new MannichiTheFeveredDream());
        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mannichi)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mannichi)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering after activation but before resolution is switched")
    void affectsCreaturesEnteringBeforeResolution() {
        harness.addToBattlefield(player1, new MannichiTheFeveredDream());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);

        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Switching can make marked damage lethal and persists after Mannichi dies")
    void markedDamageBecomesLethalAfterSwitch() {
        Permanent mannichi = harness.addToBattlefieldAndReturn(player1, new MannichiTheFeveredDream());
        Permanent raider = harness.addToBattlefieldAndReturn(player2, new AkkiRaider());
        mannichi.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Mannichi, the Fevered Dream");
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mannichi, the Fevered Dream");
        harness.assertNotOnBattlefield(player1, "Mannichi, the Fevered Dream");
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
    }
}
