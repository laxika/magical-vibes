package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownInTheLoch;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonecoilSerpent.class, Shinechaser.class, Gingerbrute.class,
        DrownInTheLoch.class, Frogify.class})
class StonecoilSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXPlusOneCounters() {
        harness.setHand(player1, List.of(new StonecoilSerpent()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Stonecoil Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 causes the 0/0 Stonecoil Serpent to die")
    void zeroCountersDiesToStateBasedActions() {
        harness.setHand(player1, List.of(new StonecoilSerpent()));

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stonecoil Serpent");
    }

    @Test
    @DisplayName("Stonecoil Serpent cannot be blocked by a multicolored creature")
    void cannotBeBlockedByMulticoloredCreature() {
        Permanent serpent = addCreatureReady(player1, new StonecoilSerpent());
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        serpent.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Shinechaser());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    void enteringWithoutBeingCastGivesNoCountersAndDies() {
        Permanent serpent = harness.enterBattlefieldAndReturn(player1, new StonecoilSerpent());

        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Stonecoil Serpent");
        harness.assertInGraveyard(player1, "Stonecoil Serpent");
    }

    @Test
    void canBlockFlyingMulticoloredCreatureAndPreventItsDamage() {
        addCreatureReady(player1, new Shinechaser());
        Permanent serpent = addCreatureReady(player2, new StonecoilSerpent());
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Stonecoil Serpent");
        assertThat(serpent.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shinechaser");
        harness.assertLife(player2, 20);
    }

    @Test
    void tramplesOverColorlessBlocker() {
        Permanent serpent = addCreatureReady(player1, new StonecoilSerpent());
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent blocker = addCreatureReady(player2, new Gingerbrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Gingerbrute");
        assertThat(serpent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void multicoloredSpellCannotTargetSerpentEvenWithEmptyGraveyard() {
        Permanent serpent = addCreatureReady(player2, new StonecoilSerpent());
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new DrownInTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, serpent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multicoloredSpellCanTargetSerpentAfterFrogifyRemovesItsAbilities() {
        Permanent serpent = addCreatureReady(player2, new StonecoilSerpent());
        serpent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Frogify(), new DrownInTheLoch()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, serpent.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Frogify");

        harness.castInstant(player1, 0, 1, serpent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stonecoil Serpent");
        harness.assertNotOnBattlefield(player2, "Stonecoil Serpent");
    }
}
