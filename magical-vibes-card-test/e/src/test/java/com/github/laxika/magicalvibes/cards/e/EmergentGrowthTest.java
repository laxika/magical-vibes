package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergentGrowth.class, QueensBaySoldier.class})
class EmergentGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Emergent Growth gives +5/+5 and sets must-be-blocked flag")
    void resolvingBoostsAndSetsMustBeBlocked() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(5);
        assertThat(bears.getToughnessModifier()).isEqualTo(5);
        assertThat(bears.getEffectivePower()).isEqualTo(7);
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
        // "must be blocked if able" is satisfied by one blocker — it must not become the Lure-style
        // "all creatures able to block it do so".
        assertThat(bears.isMustBeBlockedByAllThisTurn()).isFalse();
        assertThat(bears.isMustAttackThisTurn()).isFalse();
        assertThat(bears.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    @DisplayName("Creature with must-be-blocked flag must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent attacker = addCreatureReady(player1, new QueensBaySoldier());
        attacker.setAttacking(true);
        attacker.setMustBeBlockedThisTurn(true);

        addCreatureReady(player2, new QueensBaySoldier());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies the must-be-blocked requirement")
    void oneBlockerSuffices() {
        Permanent attacker = addCreatureReady(player1, new QueensBaySoldier());
        attacker.setAttacking(true);
        attacker.setMustBeBlockedThisTurn(true);

        addCreatureReady(player2, new QueensBaySoldier());
        addCreatureReady(player2, new QueensBaySoldier());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block")
    void tappedCreaturesNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new QueensBaySoldier());
        attacker.setAttacking(true);
        attacker.setMustBeBlockedThisTurn(true);

        Permanent tapped = addCreatureReady(player2, new QueensBaySoldier());
        tapped.tap();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Boost and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Emergent Growth fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Queen's Bay Soldier");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Emergent Growth");
    }

    @Test
    @DisplayName("Assigning the only blocker elsewhere cannot bypass Emergent Growth")
    void cannotBypassRequirementByBlockingAnotherAttacker() {
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("After resolving Emergent Growth one blocker suffices and others may block elsewhere")
    void resolvedSpellRequiresOnlyOneBlocker() {
        Permanent target = addCreatureReady(player1, new QueensBaySoldier());
        addCreatureReady(player1, new QueensBaySoldier());
        Permanent firstBlocker = addCreatureReady(player2, new QueensBaySoldier());
        Permanent secondBlocker = addCreatureReady(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Emergent Growth can target an opponent's creature without forcing it to block")
    void canTargetOpponentsCreatureWithoutForcingItToBlock() {
        addCreatureReady(player1, new QueensBaySoldier());
        Permanent target = addCreatureReady(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(target.isBlocking()).isFalse();
    }
}
