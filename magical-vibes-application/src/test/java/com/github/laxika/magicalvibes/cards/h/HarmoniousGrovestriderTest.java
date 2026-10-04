package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmoniousGrovestrider.class, Forest.class, Shock.class, RodOfRuin.class})
class HarmoniousGrovestriderTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands its controller controls")
    void powerToughnessEqualControlledLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());

        assertThat(gqs.getEffectivePower(gd, grovestrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grovestrider)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, grovestrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grovestrider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(1);

        harness.castAndResolveInstant(player2, 0, grovestrider.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Harmonious Grovestrider");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays")
    void payingWardLetsSpellResolve() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(3);

        harness.castAndResolveInstant(player2, 0, grovestrider.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grovestrider);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void unpaidWardPreventsLethalDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(1);

        harness.castAndResolveInstant(player2, 0, grovestrider.getId());

        harness.assertOnBattlefield(player1, "Harmonious Grovestrider");
        assertThat(grovestrider.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void paidWardAllowsLethalDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        prepareOpponentShock(3);

        harness.castAndResolveInstant(player2, 0, grovestrider.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Harmonious Grovestrider");
        harness.assertInGraveyard(player1, "Harmonious Grovestrider");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllerSpellDoesNotTriggerWard() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, grovestrider.getId());

        harness.assertInGraveyard(player1, "Harmonious Grovestrider");
        harness.assertNotOnBattlefield(player1, "Harmonious Grovestrider");
    }

    @Test
    void noControlledLandsCausesDeathDespiteOpponentsLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new HarmoniousGrovestrider());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Harmonious Grovestrider");
        harness.assertInGraveyard(player1, "Harmonious Grovestrider");
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        harness.addToBattlefield(player1, new Forest());
        Permanent grovestrider = harness.addToBattlefieldAndReturn(player1, new HarmoniousGrovestrider());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, grovestrider.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Harmonious Grovestrider");
        assertThat(grovestrider.getMarkedDamage()).isZero();
    }

    @Test
    void landCountDefinesPowerAndToughnessInGraveyard() {
        HarmoniousGrovestrider grovestrider = new HarmoniousGrovestrider();
        harness.setGraveyard(player1, List.of(grovestrider));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, grovestrider)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, grovestrider)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, grovestrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, grovestrider)).isEqualTo(3);
    }

    private void prepareOpponentShock(int redMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, redMana);
    }
}
