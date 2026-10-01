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

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
