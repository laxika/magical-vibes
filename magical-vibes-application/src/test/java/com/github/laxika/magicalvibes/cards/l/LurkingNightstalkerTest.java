package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingNightstalker.class, AlabornTrooper.class})
class LurkingNightstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(2);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the attacking Nightstalker gets the attack boost")
    void boostsOnlyTheAttackingNightstalker() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());
        Permanent otherCreature = addCreatureReady(player1, new AlabornTrooper());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(2);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(0);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+2/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(0);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost while it sits on the battlefield without attacking")
    void noBoostWithoutAttacking() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        assertThat(nightstalker.getPowerModifier()).isEqualTo(0);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An attacking Nightstalker does not boost another Nightstalker that stays back")
    void doesNotBoostNonattackingNightstalker() {
        Permanent attacker = addCreatureReady(player1, new LurkingNightstalker());
        Permanent nonattacker = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(nonattacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Nightstalker receives only its own boost")
    void simultaneousAttackTriggersBoostTheirOwnSources() {
        Permanent first = addCreatureReady(player1, new LurkingNightstalker());
        Permanent second = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isZero();
    }
}
