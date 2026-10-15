package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientZero.class, GrizzlyBears.class, Humility.class})
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

    @Test
    @DisplayName("Humility removes Patient Zero's damage persistence ability")
    void damageIsRemovedWhenHumilityRemovesTheAbility() {
        addCreatureReady(player1, new PatientZero());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Humility());
        opponentCreature.setMarkedDamage(1);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(opponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Marked damage survives multiple cleanup steps until Patient Zero leaves")
    void damagePersistsUntilPatientZeroLeaves() {
        Permanent source = addCreatureReady(player1, new PatientZero());
        Permanent opponentCreature = addCreatureReady(player2, new PatientZero());
        opponentCreature.setMarkedDamage(1);

        harness.inMutationScope(() -> {
            TurnCleanupService cleanup = GameTestEngineContext.get().getBean(TurnCleanupService.class);
            cleanup.resetEndOfTurnModifiers(gd);
            cleanup.resetEndOfTurnModifiers(gd);
        });
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(opponentCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Patient Zero gains life when it deals combat damage")
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player1, new PatientZero());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(java.util.List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
