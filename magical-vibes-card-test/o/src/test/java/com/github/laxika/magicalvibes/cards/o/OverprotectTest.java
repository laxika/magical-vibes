package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overprotect.class, GrizzlyBears.class, GiantGrowth.class})
class OverprotectTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature you control and grants trample, hexproof, and indestructible")
    void boostsAndGrantsKeywords() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(creature.getGrantedKeywords())
                .contains(Keyword.TRAMPLE, Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The boost and granted keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords())
                .doesNotContain(Keyword.TRAMPLE, Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overprotect()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting the protected creature")
    void hexproofPreventsOpponentTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castResolve(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void indestructiblePreventsDestructionFromLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        creature.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void hexproofStillAllowsControllerToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castResolve(creature);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    void doesNotResolveWhenTargetIsNoLongerControlledByCaster() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overprotect()));
        addMana();
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getGrantedKeywords())
                .doesNotContain(Keyword.TRAMPLE, Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Overprotect");
    }

    private void castResolve(Permanent target) {
        harness.setHand(player1, List.of(new Overprotect()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
