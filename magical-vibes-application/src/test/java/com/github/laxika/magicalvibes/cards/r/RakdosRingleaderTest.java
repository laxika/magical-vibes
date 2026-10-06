package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosRingleader.class, DrudgeBeetle.class, Forest.class, ToweringIndrik.class})
class RakdosRingleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card at random")
    void combatDamageMakesDamagedPlayerDiscardAtRandom() {
        addAttackingRingleader(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new DrudgeBeetle(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("at random"));
    }

    @Test
    @DisplayName("No discard trigger when blocked and dealing no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingRingleader(player1);
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        // No combat damage reached the player, so no random discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("discards") && log.contains("at random"));
    }

    @Test
    @DisplayName("Resolving regenerate grants a regeneration shield")
    void resolvingRegenerateGrantsShield() {
        addCreatureReady(player1, new RakdosRingleader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Rakdos Ringleader from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // The 2/4 attacker survives first-strike damage and deals lethal damage in the normal step.
        Permanent ringleader = addCreatureReady(player1, new RakdosRingleader());
        ringleader.setRegenerationShield(1);
        ringleader.setBlocking(true);
        ringleader.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new ToweringIndrik());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Rakdos Ringleader");
        Permanent survivor = findPermanent(player1, "Rakdos Ringleader");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player with an empty hand resolves without a discard choice")
    void combatDamageToEmptyHand() {
        addAttackingRingleader(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal lethal damage back")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        addAttackingRingleader(player1);
        Permanent blocker = addCreatureReady(player2, new DrudgeBeetle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        harness.assertOnBattlefield(player1, "Rakdos Ringleader");
        harness.assertInGraveyard(player2, "Drudge Beetle");
    }

    private Permanent addAttackingRingleader(Player player) {
        Permanent ringleader = addCreatureReady(player, new RakdosRingleader());
        ringleader.setAttacking(true);
        return ringleader;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
