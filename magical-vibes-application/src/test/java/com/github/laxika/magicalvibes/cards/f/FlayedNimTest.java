package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.s.SteelWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlayedNim.class, FistsOfTheAnvil.class, ElectrostaticBolt.class, SteelWall.class, AlphaMyr.class})
class FlayedNimTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration ability creates a regeneration shield")
    void regenerationAbilityCreatesShield() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nim.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield prevents lethal damage from destroying Flayed Nim")
    void regenerationShieldPreventsLethalDamage() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nim.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nim);
        assertThat(nim.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Combat damage causes the damaged creature's controller to lose that much life")
    void combatDamageCausesLifeLossEqualToDamage() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, nim.getId());

        Permanent blocker = addCreatureReady(player2, new SteelWall());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("The ability triggers only when Flayed Nim itself deals the combat damage")
    void anotherCreatureDealingCombatDamageDoesNotTriggerLifeLoss() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        addCreatureReady(player1, new AlphaMyr());
        Permanent blocker = addCreatureReady(player2, new SteelWall());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nim);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The ability does not trigger from noncombat damage")
    void noncombatDamageDoesNotTriggerLifeLoss() {
        addCreatureReady(player1, new FlayedNim());
        Permanent target = addCreatureReady(player2, new SteelWall());
        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the creature-damage ability")
    void combatDamageToPlayerDoesNotTriggerLifeLoss() {
        addCreatureReady(player1, new FlayedNim());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The ability still triggers if Flayed Nim dies after dealing combat damage")
    void combatDamageTriggerSurvivesSourceDeath() {
        Permanent nim = addCreatureReady(player1, new FlayedNim());
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new FistsOfTheAnvil()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, nim.getId());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nim);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
