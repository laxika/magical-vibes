package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowDogs.class})
class HollowDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && dogs.getId().equals(e.getSourcePermanentId()));
    }

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void getsBoostWhenAttacking() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dogs.getPowerModifier()).isEqualTo(2);
        assertThat(dogs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the attacking creature gets the attack boost")
    void onlyAttackingCreatureGetsBoost() {
        Permanent attackingDogs = addCreatureReady(player1, new HollowDogs());
        Permanent restingDogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attackingDogs.getPowerModifier()).isEqualTo(2);
        assertThat(restingDogs.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dogs = addCreatureReady(player1, new HollowDogs());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(dogs.getPowerModifier()).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(dogs.getPowerModifier()).isEqualTo(0);
        assertThat(dogs.getToughnessModifier()).isEqualTo(0);
    }
}
