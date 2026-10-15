package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VectorAsp.class, MoriokReaver.class})
class VectorAspTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Vector Asp puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VectorAsp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    @Test
    @DisplayName("Resolving puts Vector Asp onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new VectorAsp()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Vector Asp");
    }

    @Test
    @DisplayName("Activating infect ability puts it on the stack")
    void activatingInfectPutsOnStack() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vector Asp");
        assertThat(entry.getTargetId()).isEqualTo(asp.getId());
    }

    @Test
    @DisplayName("Resolving infect ability grants infect until end of turn")
    void resolvingInfectAbilityGrantsInfect() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Infect granted by ability resets at end of turn cleanup")
    void infectResetsAtEndOfTurn() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Activating ability does NOT tap Vector Asp")
    void activatingAbilityDoesNotTap() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(asp.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without black mana")
    void cannotActivateWithoutBlackMana() {
        addAspReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent asp = addAspReady(player1);
        asp.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new VectorAsp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vector Asp");
    }

    @Test
    @DisplayName("Vector Asp with infect deals poison counters to defending player when unblocked")
    void dealsPoison() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Activate infect ability and resolve it
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        asp.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Infect does not deal regular damage to players
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Vector Asp without infect deals regular damage to defending player")
    void dealsRegularDamageWithoutInfect() {
        harness.setLife(player2, 20);
        Permanent asp = addAspReady(player1);
        asp.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability resolves without granting infect if Vector Asp has left the battlefield")
    void abilityHasNoEffectIfSourceRemoved() {
        addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Infect combat damage leaves permanent minus counters on a surviving blocker")
    void infectDamageToCreaturePersistsAfterCleanup() {
        Permanent asp = addAspReady(player1);
        Permanent blocker = addCreatureReady(player2, new MoriokReaver());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        asp.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Moriok Reaver");
        harness.assertInGraveyard(player1, "Vector Asp");
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated infect activations do not multiply poison counters")
    void repeatedActivationsDoNotMultiplyPoison() {
        Permanent asp = addAspReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, asp, Keyword.INFECT)).isTrue();
        asp.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    private Permanent addAspReady(Player player) {
        return addCreatureReady(player, new VectorAsp());
    }
}
