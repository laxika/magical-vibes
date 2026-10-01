package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingInferno.class, Gristleback.class, IzzetSignet.class})
class LivingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Divides its power among target creatures and receives damage from each")
    void dividesDamageAndReceivesDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent firstGristleback = addCreatureReady(player2, new Gristleback());
        Permanent secondGristleback = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(firstGristleback.getId(), 4, secondGristleback.getId(), 4));

        assertThat(inferno.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Gristleback");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new LivingInferno());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(signet.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a target when dividing nonzero damage")
    void requiresATargetWhenDividingNonzeroDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(inferno.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires assigning all of its power as damage")
    void requiresAssigningAllPowerAsDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(target.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(inferno.isTapped()).isFalse();
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
