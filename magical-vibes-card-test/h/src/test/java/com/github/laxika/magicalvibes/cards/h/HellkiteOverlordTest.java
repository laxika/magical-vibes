package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteOverlord.class})
class HellkiteOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("{R} ability gives +1/+0 until end of turn")
    void firebreathingBoostsPower() {
        Permanent overlord = addCreatureReady(player1, new HellkiteOverlord());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(1);
        assertThat(overlord.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{R} boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent overlord = addCreatureReady(player1, new HellkiteOverlord());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(0);
        assertThat(overlord.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("{B}{G} ability grants a regeneration shield")
    void regenerationGrantsShield() {
        Permanent overlord = addCreatureReady(player1, new HellkiteOverlord());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield does not tap or heal until destruction is replaced")
    void shieldIsConsumedOnlyWhenLethalDamageWouldDestroyOverlord() {
        Permanent overlord = addCreatureReady(player1, new HellkiteOverlord());
        overlord.setMarkedDamage(3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(overlord.isTapped()).isFalse();
        assertThat(overlord.getMarkedDamage()).isEqualTo(3);

        overlord.setMarkedDamage(8);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Hellkite Overlord");
        assertThat(overlord.isTapped()).isTrue();
        assertThat(overlord.getMarkedDamage()).isZero();
        assertThat(overlord.getRegenerationShield()).isZero();

        overlord.setMarkedDamage(8);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Hellkite Overlord");
        harness.assertInGraveyard(player1, "Hellkite Overlord");
    }

    @Test
    @DisplayName("Multiple regeneration shields expire at cleanup")
    void unusedRegenerationShieldsExpireAtEndOfTurn() {
        Permanent overlord = addCreatureReady(player1, new HellkiteOverlord());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getRegenerationShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(overlord.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent overlord = harness.addToBattlefieldAndReturn(player1, new HellkiteOverlord());
        overlord.setSummoningSick(true);
        overlord.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(overlord.getPowerModifier()).isEqualTo(1);
        assertThat(overlord.getRegenerationShield()).isEqualTo(1);
        assertThat(overlord.isTapped()).isTrue();
    }
}
