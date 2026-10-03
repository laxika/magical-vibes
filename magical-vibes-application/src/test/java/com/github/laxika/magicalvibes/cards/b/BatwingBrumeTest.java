package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.f.FlameJab;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatwingBrume.class, NettleSentinel.class, FlameJab.class})
class BatwingBrumeTest extends BaseCardTest {

    @Test
    @DisplayName("{W} spent: prevents all combat damage this turn")
    void whiteSpentPreventsCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker(player1, new NettleSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.WHITE, 2); // {1}{W/B} both paid white → only {W} spent
        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(TurnStep.END_COMBAT);

        // Attacker's 2 damage to player2 is prevented; no black spent so no drain.
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("{B} spent: each player loses 1 life per attacking creature they control")
    void blackSpentDrainsPerAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // Only the active player's attacking creatures count, not idle creatures.
        addAttacker(player1, new NettleSentinel());
        addAttacker(player1, new NettleSentinel());
        addCreatureReady(player1, new NettleSentinel());
        addCreatureReady(player2, new NettleSentinel());

        // Creatures remain attacking in the end of combat step, after damage has been dealt.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.BLACK, 2); // only {B} spent
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 18); // two attackers
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("{B} spent alone does not prevent combat damage")
    void blackSpentDoesNotPreventCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker(player1, new NettleSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.BLACK, 2); // only {B} spent → no prevention
        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(TurnStep.END_COMBAT);

        // Drain: player1 loses 1 for its lone attacker; that attacker's 2 combat damage still lands.
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("White and black spent: defender prevents combat damage and attacker loses life")
    void bothColorsSpentApplyBothEffects() {
        addAttacker(player1, new NettleSentinel());
        addAttacker(player1, new NettleSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new BatwingBrume()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);
        harness.passUntil(TurnStep.END_COMBAT);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Black spent with no attackers does not cause life loss")
    void noAttackersCauseNoLifeLoss() {
        addCreatureReady(player1, new NettleSentinel());
        addCreatureReady(player2, new NettleSentinel());
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White spent prevents damage from attackers and blockers to creatures")
    void whiteSpentPreventsDamageToCreatures() {
        Permanent attacker = addCreatureReady(player1, new NettleSentinel());
        Permanent blocker = addCreatureReady(player2, new NettleSentinel());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new BatwingBrume()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0);
        harness.passUntil(TurnStep.END_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White spent does not prevent noncombat damage to players or creatures")
    void whiteSpentDoesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player2, new NettleSentinel());
        harness.setHand(player1, List.of(new BatwingBrume(), new FlameJab(), new FlameJab()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("White combat damage prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        addAttacker(player2, new NettleSentinel());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

    private void addAttacker(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        perm.setAttackTarget(player.equals(player1) ? player2.getId() : player1.getId());
    }
}
