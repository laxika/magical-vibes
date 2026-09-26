package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoblinDynamo;
import com.github.laxika.magicalvibes.cards.w.WallOfDeceit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrippingDead.class, EnormousBaloth.class, FugitiveWizard.class, GoblinDynamo.class,
        WallOfDeceit.class})
class DrippingDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature and prevents regeneration")
    void combatDamageDestroysDamagedCreatureWithoutRegeneration() {
        Permanent drippingDead = addCreatureReady(player1, new DrippingDead());
        Permanent wall = addCreatureReady(player2, new WallOfDeceit());
        wall.setRegenerationShield(1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dripping Dead");
        harness.assertNotOnBattlefield(player2, "Wall of Deceit");
        harness.assertInGraveyard(player2, "Wall of Deceit");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger the destruction ability")
    void noncombatDamageDoesNotDestroy() {
        addCreatureReady(player1, new DrippingDead());
        Permanent dynamo = addCreatureReady(player1, new GoblinDynamo());
        Permanent wall = addCreatureReady(player2, new WallOfDeceit());

        harness.activateAbility(player1, 1, null, wall.getId());
        harness.passBothPriorities();

        assertThat(dynamo.isTapped()).isTrue();
        assertThat(wall.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Wall of Deceit");
    }

    @Test
    @DisplayName("Combat damage from another creature you control does not trigger Dripping Dead")
    void damageFromAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new DrippingDead());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        Permanent wall = addCreatureReady(player2, new WallOfDeceit());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Wall of Deceit");
        assertThat(wall.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The destruction trigger still resolves when Dripping Dead dies in combat")
    void triggerResolvesAfterDrippingDeadDies() {
        addCreatureReady(player1, new DrippingDead());
        Permanent blocker = addCreatureReady(player2, new EnormousBaloth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Dripping Dead");
        harness.assertNotOnBattlefield(player2, "Enormous Baloth");
        harness.assertInGraveyard(player2, "Enormous Baloth");
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Dripping Dead cannot be declared as a blocker")
    void cannotBlock() {
        addCreatureReady(player1, new FugitiveWizard()).setAttacking(true);
        addCreatureReady(player2, new DrippingDead());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
