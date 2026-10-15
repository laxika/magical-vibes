package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.t.ThopterAssembly;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleshEaterImp.class, PhyrexianRager.class, ThopterAssembly.class})
class FleshEaterImpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent impPerm = addCreatureReady(player1, new FleshEaterImp());
        harness.addToBattlefield(player1, new PhyrexianRager());
        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);

        // Phyrexian Rager should be sacrificed
        harness.assertNotOnBattlefield(player1, "Phyrexian Rager");
        harness.assertInGraveyard(player1, "Phyrexian Rager");

        // Flesh-Eater Imp should still be on the battlefield
        harness.assertOnBattlefield(player1, "Flesh-Eater Imp");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(impPerm.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Flesh-Eater Imp +1/+1")
    void resolvingAbilityBoostsImp() {
        addCreatureReady(player1, new FleshEaterImp());
        harness.addToBattlefield(player1, new PhyrexianRager());
        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(imp.getPowerModifier()).isEqualTo(1);
        assertThat(imp.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new FleshEaterImp());
        harness.addToBattlefield(player1, new PhyrexianRager());
        Permanent secondRager = harness.addToBattlefieldAndReturn(player1, new PhyrexianRager());

        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);
        harness.passBothPriorities();

        UUID secondRagerId = secondRager.getId();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondRagerId);
        harness.passBothPriorities();

        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getCard().getName()).isEqualTo("Flesh-Eater Imp");
        assertThat(imp.getPowerModifier()).isEqualTo(2);
        assertThat(imp.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice Flesh-Eater Imp to its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new FleshEaterImp());

        harness.activateAbility(player1, 0, null, null);

        // Imp should be sacrificed
        harness.assertNotOnBattlefield(player1, "Flesh-Eater Imp");
        harness.assertInGraveyard(player1, "Flesh-Eater Imp");

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new FleshEaterImp());
        harness.addToBattlefield(player1, new PhyrexianRager());
        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);
        harness.passBothPriorities();

        Permanent imp = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(imp.getPowerModifier()).isEqualTo(1);
        assertThat(imp.getToughnessModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(imp.getPowerModifier()).isEqualTo(0);
        assertThat(imp.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated without mana")
    void canActivateWithoutMana() {
        addCreatureReady(player1, new FleshEaterImp());
        harness.addToBattlefield(player1, new PhyrexianRager());
        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability even when Imp is tapped")
    void canActivateWhenTapped() {
        Permanent impPerm = addCreatureReady(player1, new FleshEaterImp());
        impPerm.tap();
        harness.addToBattlefield(player1, new PhyrexianRager());
        UUID ragerId = harness.getPermanentId(player1, "Phyrexian Rager");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ragerId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("When Imp is the only creature, it auto-sacrifices itself")
    void autoSacrificesWhenOnlyCreature() {
        addCreatureReady(player1, new FleshEaterImp());

        harness.activateAbility(player1, 0, null, null);

        // Imp should be auto-sacrificed (only creature available)
        harness.assertNotOnBattlefield(player1, "Flesh-Eater Imp");
        harness.assertInGraveyard(player1, "Flesh-Eater Imp");

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Flesh-Eater Imp");
    }

    @Test
    @DisplayName("Boosted unblocked Imp deals poison counters instead of life loss")
    void boostedImpDealsPoison() {
        addCreatureReady(player1, new FleshEaterImp());
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new PhyrexianRager());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, rager.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Imp deals minus counters to a flying blocker instead of marked damage")
    void infectDamageToFlyingBlocker() {
        addCreatureReady(player1, new FleshEaterImp());
        Permanent blocker = addCreatureReady(player2, new ThopterAssembly());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flesh-Eater Imp");
        harness.assertOnBattlefield(player2, "Thopter Assembly");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Imp")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new FleshEaterImp());
        addCreatureReady(player2, new PhyrexianRager());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Imp does not boost another Imp when the ability resolves")
    void sacrificedSourceDoesNotBoostAnotherImp() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FleshEaterImp());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FleshEaterImp());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        harness.assertInGraveyard(player1, "Flesh-Eater Imp");
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

}
