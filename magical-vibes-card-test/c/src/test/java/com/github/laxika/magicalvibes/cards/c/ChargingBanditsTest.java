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

@CardUsed({ChargingBandits.class})
class ChargingBanditsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new ChargingBandits());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Charging Bandits"));
    }

    @Test
    @DisplayName("Gets +2/+0 when attacking and trigger resolves")
    void boostsOnAttack() {
        Permanent bandits = addCreatureReady(player1, new ChargingBandits());

        declareAttackers(player1, List.of(0));
        assertThat(PermanentGrants.powerBoostOf(gd, bandits)).isZero();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bandits)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bandits)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the attacking Bandits gets its own boost")
    void onlyAttackingBanditsGetsBoost() {
        Permanent attackingBandits = addCreatureReady(player1, new ChargingBandits());
        Permanent restingBandits = addCreatureReady(player1, new ChargingBandits());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingBandits)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, restingBandits)).isZero();
    }

    @Test
    @DisplayName("+2/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent bandits = addCreatureReady(player1, new ChargingBandits());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, bandits)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(PermanentGrants.powerBoostOf(gd, bandits)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bandits)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each attacking Bandits receives exactly its own boost")
    void simultaneousAttackersEachGetTheirOwnBoost() {
        Permanent first = addCreatureReady(player1, new ChargingBandits());
        Permanent second = addCreatureReady(player1, new ChargingBandits());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attack trigger cannot boost a new permanent representing the same card")
    void sourceLeavingAndReturningDoesNotReceiveOldBoost() {
        ChargingBandits card = new ChargingBandits();
        Permanent original = addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = addCreatureReady(player1, card);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, returned)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, returned)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
