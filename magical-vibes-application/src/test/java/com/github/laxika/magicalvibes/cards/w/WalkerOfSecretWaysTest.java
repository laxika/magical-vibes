package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalkerOfSecretWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Walker in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WalkerOfSecretWays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent walker = findPermanent(player1, "Walker of Secret Ways");
        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.isAttacking()).isTrue();
        assertThat(walker.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Combat damage to a player lets its controller look at that player's hand")
    void combatDamageLooksAtDamagedPlayersHand() {
        Permanent walker = addCreatureReady(player1, new WalkerOfSecretWays());
        walker.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("{1}{U} bounces a Ninja you control, and a non-Ninja is an illegal target")
    void bouncesOnlyNinjasYouControl() {
        Permanent walker = addCreatureReady(player1, new WalkerOfSecretWays());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, walker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Walker of Secret Ways");
        harness.assertNotOnBattlefield(player1, "Walker of Secret Ways");
    }

    @Test
    @DisplayName("A Ninja an opponent controls is an illegal target")
    void cannotBounceOpponentsNinja() {
        addCreatureReady(player1, new WalkerOfSecretWays());
        Permanent theirs = addCreatureReady(player2, new WalkerOfSecretWays());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, theirs.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The bounce ability can't be activated during an opponent's turn")
    void bounceIsRestrictedToYourTurn() {
        Permanent walker = addCreatureReady(player1, new WalkerOfSecretWays());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, walker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
