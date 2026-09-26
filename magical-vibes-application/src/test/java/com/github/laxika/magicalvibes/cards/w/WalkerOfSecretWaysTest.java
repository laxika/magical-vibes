package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkerOfSecretWays.class, Frostling.class})
class WalkerOfSecretWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Walker in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.setHand(player1, List.of(new WalkerOfSecretWays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frostling");
        Permanent walker = findPermanent(player1, "Walker of Secret Ways");
        assertThat(walker.isTapped()).isTrue();
        assertThat(walker.isAttacking()).isTrue();
        assertThat(walker.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Ninjutsu can't return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WalkerOfSecretWays()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unblocked attacker");
    }

    @Test
    @DisplayName("Combat damage to a player lets its controller look at that player's hand")
    void combatDamageLooksAtDamagedPlayersHand() {
        Permanent walker = addCreatureReady(player1, new WalkerOfSecretWays());
        walker.setAttacking(true);
        harness.setHand(player2, new ArrayList<>(List.of(new Frostling())));

        resolveCombat();
        resolveAllTriggers();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Frostling"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Frostling"));
    }

    @Test
    @DisplayName("A blocked Walker does not look at the defending player's hand")
    void blockedWalkerDoesNotTriggerHandInspection() {
        addCreatureReady(player1, new WalkerOfSecretWays());
        addCreatureReady(player2, new Frostling());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        harness.setHand(player2, new ArrayList<>(List.of(new Frostling())));

        resolveCombat();
        resolveAllTriggers();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("{1}{U} bounces a Ninja you control, and a non-Ninja is an illegal target")
    void bouncesOnlyNinjasYouControl() {
        Permanent walker = addCreatureReady(player1, new WalkerOfSecretWays());
        Permanent frostling = addCreatureReady(player1, new Frostling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, frostling.getId()))
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
