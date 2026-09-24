package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientZero.class, GrizzlyBears.class})
class PatientZeroTest extends BaseCardTest {

    @Test
    @DisplayName("Damage remains marked on opponents' creatures through cleanup")
    void damageRemainsMarkedOnOpponentsCreaturesThroughCleanup() {
        addCreatureReady(player1, new PatientZero());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setMarkedDamage(1);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is removed from creatures controlled by Patient Zero's controller")
    void damageIsRemovedFromControllersCreatures() {
        addCreatureReady(player1, new PatientZero());
        Permanent controllerCreature = addCreatureReady(player1, new GrizzlyBears());
        controllerCreature.setMarkedDamage(1);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(controllerCreature.getMarkedDamage()).isZero();
    }
}
