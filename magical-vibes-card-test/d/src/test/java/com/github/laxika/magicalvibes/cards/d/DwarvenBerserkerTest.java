package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Avizoa;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenBerserker.class, Avizoa.class, Thunderbolt.class})
class DwarvenBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked gives +3/+0 and trample")
    void becomingBlockedPumpsAndGrantsTrample() {
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, new Avizoa());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isZero();
        assertThat(berserker.getEffectivePower()).isEqualTo(4);
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Multiple blockers still give only +3/+0 once")
    void multipleBlockersTriggerOnce() {
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, new Avizoa());
        addCreatureReady(player2, new Avizoa());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("If unblocked nothing triggers")
    void unblockedCreatesNoTrigger() {
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, new Avizoa());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.getToughnessModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Avizoa());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Blocking another creature does not grant the bonus")
    void blockingDoesNotTriggerAbility() {
        Permanent attacker = addCreatureReady(player1, new DwarvenBerserker());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DwarvenBerserker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The bonus still resolves after the blocker leaves the battlefield")
    void blockerLeavingDoesNotPreventBonus() {
        Permanent berserker = addCreatureReady(player1, new DwarvenBerserker());
        berserker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Avizoa());
        harness.setHand(player1, List.of(new Thunderbolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.castModalInstant(player1, 0, 1, List.of(blocker.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Avizoa");
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isZero();
        assertThat(berserker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
