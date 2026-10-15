package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessingOfTheNephilim;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.SealOfDoom;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaazdaExonerator.class, BlessingOfTheNephilim.class, SealOfDoom.class, MistralCharger.class})
class HaazdaExoneratorTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetAura() {
        addCreatureReady(player1, new HaazdaExonerator());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlessingOfTheNephilim());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Haazda Exonerator");
        harness.assertNotOnBattlefield(player1, "Blessing of the Nephilim");
        harness.assertInGraveyard(player1, "Haazda Exonerator");
        harness.assertInGraveyard(player1, "Blessing of the Nephilim");
    }

    @Test
    void canDestroyAuraControlledByOpponent() {
        addCreatureReady(player1, new HaazdaExonerator());
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BlessingOfTheNephilim());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Haazda Exonerator");
        harness.assertNotOnBattlefield(player2, "Blessing of the Nephilim");
        harness.assertInGraveyard(player1, "Haazda Exonerator");
        harness.assertInGraveyard(player2, "Blessing of the Nephilim");
    }

    @Test
    void cannotTargetNonAuraEnchantment() {
        addCreatureReady(player1, new HaazdaExonerator());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new SealOfDoom());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Aura");
        harness.assertOnBattlefield(player1, "Haazda Exonerator");
    }

    @Test
    void sacrificeIsPaidBeforeAuraIsDestroyed() {
        addCreatureReady(player1, new HaazdaExonerator());
        Permanent creature = addCreatureReady(player2, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BlessingOfTheNephilim());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, aura.getId());

        harness.assertInGraveyard(player1, "Haazda Exonerator");
        harness.assertNotOnBattlefield(player1, "Haazda Exonerator");
        harness.assertOnBattlefield(player2, "Blessing of the Nephilim");
        harness.assertNotInGraveyard(player2, "Blessing of the Nephilim");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Blessing of the Nephilim");
        harness.assertOnBattlefield(player2, "Mistral Charger");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new HaazdaExonerator());
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlessingOfTheNephilim());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Haazda Exonerator");
        harness.assertOnBattlefield(player1, "Blessing of the Nephilim");
        harness.assertNotInGraveyard(player1, "Haazda Exonerator");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent exonerator = addCreatureReady(player1, new HaazdaExonerator());
        exonerator.tap();
        Permanent creature = addCreatureReady(player1, new MistralCharger());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlessingOfTheNephilim());
        aura.setAttachedTo(creature.getId());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Haazda Exonerator");
        harness.assertOnBattlefield(player1, "Blessing of the Nephilim");
        harness.assertNotInGraveyard(player1, "Haazda Exonerator");
    }

    @Test
    void canTargetAuraAttachedToItselfWithoutRefundingSacrifice() {
        Permanent exonerator = addCreatureReady(player1, new HaazdaExonerator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlessingOfTheNephilim());
        aura.setAttachedTo(exonerator.getId());
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Haazda Exonerator");
        harness.assertNotOnBattlefield(player1, "Blessing of the Nephilim");
        harness.assertInGraveyard(player1, "Haazda Exonerator");
        harness.assertInGraveyard(player1, "Blessing of the Nephilim");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
