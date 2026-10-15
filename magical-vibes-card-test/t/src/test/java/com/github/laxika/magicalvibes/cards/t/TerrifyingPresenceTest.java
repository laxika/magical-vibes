package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorderlandRanger;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerrifyingPresence.class, BorderlandRanger.class, ScrollOfAvacyn.class})
class TerrifyingPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can still deal combat damage")
    void targetCreatureStillDealsCombatDamage() {
        Permanent target = addCreature(player1);
        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isFalse();
    }

    @Test
    @DisplayName("Every other creature is prevented from dealing combat damage")
    void otherCreaturesPreventedFromDealingCombatDamage() {
        Permanent target = addCreature(player1);
        Permanent otherOwn = addCreature(player1);
        Permanent opponent = addCreature(player2);

        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, otherOwn, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, opponent, true)).isTrue();
    }

    @Test
    @DisplayName("Non-combat damage from other creatures is unaffected")
    void nonCombatDamageIsUnaffected() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);

        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, false)).isFalse();
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);
        castTerrifyingPresence(target);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, true)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ScrollOfAvacyn());
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = addCreature(player1);
        castTerrifyingPresence(target);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Terrifying Presence");
    }

    @Test
    @DisplayName("Only the exempt attacker deals damage to the defending player")
    void onlyTargetAttackerDealsCombatDamage() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player1);
        castTerrifyingPresence(target);

        target.setAttacking(true);
        target.setAttackTarget(player2.getId());
        other.setAttacking(true);
        other.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player2, 20);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An exempt blocker deals damage while the blocked attacker deals none")
    void exemptBlockerKillsAttackerWithoutTakingDamage() {
        Permanent attacker = addCreature(player1);
        Permanent blocker = addCreature(player2);
        castTerrifyingPresence(blocker);

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Borderland Ranger");
    }

    @Test
    @DisplayName("An opponent's creature can be the exempt target")
    void opponentCreatureCanBeExempt() {
        Permanent own = addCreature(player1);
        Permanent target = addCreature(player2);
        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, own, true)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution are also prevented")
    void laterCreaturesArePrevented() {
        Permanent target = addCreature(player1);
        castTerrifyingPresence(target);
        Permanent laterCreature = addCreature(player2);

        assertThat(gqs.isPreventedFromDealingDamage(gd, laterCreature, true)).isTrue();
    }

    @Test
    @DisplayName("An illegal target on resolution prevents the spell from taking effect")
    void illegalTargetOnResolutionCreatesNoPrevention() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, true)).isFalse();
        harness.assertInGraveyard(player1, "Terrifying Presence");
    }

    @Test
    @DisplayName("Removing the target after resolution does not remove prevention")
    void targetLeavingAfterResolutionDoesNotEndPrevention() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);
        castTerrifyingPresence(target);
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, true)).isTrue();
    }

    @Test
    @DisplayName("Two casts with different targets prevent both targets from dealing combat damage")
    void differentExemptionsDoNotOverrideEachOther() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player2);
        castTerrifyingPresence(first);
        castTerrifyingPresence(second);

        assertThat(gqs.isPreventedFromDealingDamage(gd, first, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, second, true)).isTrue();
    }

    private void castTerrifyingPresence(Permanent target) {
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new BorderlandRanger());
    }
}
