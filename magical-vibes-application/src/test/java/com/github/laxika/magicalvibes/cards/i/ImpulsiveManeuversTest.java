package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImpulsiveManeuversTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers once for each creature that attacks, including an opponent's creature")
    void triggersForEachAttacker() {
        addReady(player1, new ImpulsiveManeuvers());
        Permanent attacker1 = addReady(player2, new GrizzlyBears());
        Permanent attacker2 = addReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Impulsive Maneuvers"));
        assertThat(gd.stack).extracting(entry -> entry.getTargetId())
                .containsExactlyInAnyOrder(attacker1.getId(), attacker2.getId());
    }

    @Test
    @DisplayName("The coin flip doubles or prevents the attacker's next combat damage")
    void nextCombatDamageIsDoubledOrPrevented() {
        harness.setLife(player2, 20);
        addReady(player1, new ImpulsiveManeuvers());
        Permanent attacker = addReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        boolean wonFlip = coinFlipWon();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, wonFlip ? 16 : 20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The combat-only shield does not consume a noncombat damage event")
    void combatOnlyShieldDoesNotAffectNoncombatDamage() {
        harness.setLife(player2, 20);
        addReady(player1, new ImpulsiveManeuvers());
        Permanent attacker = addReady(player1, new ProdigalPyromancer());
        Permanent victim = addReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        boolean wonFlip = coinFlipWon();

        attacker.untap();
        harness.activateAbility(player1, 1, null, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.getMarkedDamage()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, wonFlip ? 18 : 20);
    }

    private boolean coinFlipWon() {
        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Impulsive Maneuvers"));
        return logs.stream().anyMatch(log -> log.contains("wins the coin flip for Impulsive Maneuvers"));
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
