package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ChargingPaladin.class)
class ChargingPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new ChargingPaladin());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Charging Paladin"));
    }

    @Test
    @DisplayName("Gets +0/+3 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent paladin = addCreatureReady(player1, new ChargingPaladin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the attacking creature is boosted after its trigger resolves")
    void boostsOnlyAttackingCreatureAfterResolution() {
        Permanent paladin = addCreatureReady(player1, new ChargingPaladin());
        Permanent nonattackingPaladin = addCreatureReady(player1, new ChargingPaladin());

        declareAttackers(player1, List.of(0));
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonattackingPaladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattackingPaladin)).isEqualTo(0);

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattackingPaladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattackingPaladin)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each attacking Paladin gets its own +0/+3 bonus")
    void multipleAttackersEachGetTheirOwnBonus() {
        Permanent first = addCreatureReady(player1, new ChargingPaladin());
        Permanent second = addCreatureReady(player1, new ChargingPaladin());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("+0/+3 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent paladin = addCreatureReady(player1, new ChargingPaladin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(0);
    }
}
