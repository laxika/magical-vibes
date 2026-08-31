package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingInferno.class, GrizzlyBears.class, FountainOfYouth.class})
class LivingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Divides its power among target creatures and receives damage from each")
    void dividesDamageAndReceivesDamage() {
        Permanent inferno = addReadyInferno(player1);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(firstBear.getId(), 4, secondBear.getId(), 4));

        assertThat(inferno.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyInferno(player1);
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(fountain.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyInferno(Player player) {
        Permanent inferno = new Permanent(new LivingInferno());
        inferno.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(inferno);
        return inferno;
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
