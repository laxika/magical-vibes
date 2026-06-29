package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BelligerentBrontodonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has AssignCombatDamageWithToughnessEffect with ALL_OWN_CREATURES scope")
    void hasCorrectStaticEffect() {
        BelligerentBrontodon card = new BelligerentBrontodon();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof AssignCombatDamageWithToughnessEffect)
                .hasSize(1);
        AssignCombatDamageWithToughnessEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof AssignCombatDamageWithToughnessEffect)
                .map(e -> (AssignCombatDamageWithToughnessEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.scope()).isEqualTo(GrantScope.ALL_OWN_CREATURES);
    }

    // ===== Brontodon itself uses toughness =====

    @Test
    @DisplayName("Brontodon (4/6) deals 6 combat damage (its toughness)")
    void brontodonUsesToughnessForOwnDamage() {
        Permanent brontodon = addReadyCreature(player1, new BelligerentBrontodon());

        assertThat(gqs.getEffectivePower(gd, brontodon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brontodon)).isEqualTo(6);
        assertThat(gqs.getEffectiveCombatDamage(gd, brontodon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Brontodon attacking unblocked deals toughness damage to opponent")
    void brontodonUnblockedDealsToughnessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent brontodon = addReadyCreature(player1, new BelligerentBrontodon());
        brontodon.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14); // 20 - 6
    }

    // ===== Other creatures use toughness (toughness > power) =====

    @Test
    @DisplayName("Giant Spider (2/4) with Brontodon deals 4 combat damage")
    void creatureWithHigherToughnessUsesToughness() {
        addReadyCreature(player1, new BelligerentBrontodon());
        Permanent spider = addReadyCreature(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
    }

    // ===== Creatures with power > toughness still use toughness =====

    @Test
    @DisplayName("Goblin Piker (2/1) with Brontodon deals 1 combat damage (toughness, not power)")
    void creatureWithHigherPowerStillUsesToughness() {
        addReadyCreature(player1, new BelligerentBrontodon());
        Permanent piker = addReadyCreature(player1, new GoblinPiker());

        assertThat(gqs.getEffectivePower(gd, piker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, piker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Piker (2/1) unblocked deals 1 damage with Brontodon")
    void highPowerCreatureDealsReducedDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addReadyCreature(player1, new BelligerentBrontodon());
        Permanent piker = addReadyCreature(player1, new GoblinPiker());
        piker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1
    }

    // ===== Creatures with equal power/toughness =====

    @Test
    @DisplayName("Grizzly Bears (2/2) with Brontodon deals 2 combat damage (unchanged)")
    void creatureWithEqualPowerToughnessUnchanged() {
        addReadyCreature(player1, new BelligerentBrontodon());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
    }

    // ===== Opponent's creatures not affected =====

    @Test
    @DisplayName("Opponent's creatures are not affected by Brontodon")
    void opponentCreaturesNotAffected() {
        addReadyCreature(player1, new BelligerentBrontodon());
        Permanent opponentPiker = addReadyCreature(player2, new GoblinPiker());

        // Opponent's Goblin Piker (2/1) still deals 2 (its power)
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(2);
    }

    // ===== Blocking interactions =====

    @Test
    @DisplayName("Controlled blocker uses toughness for combat damage")
    void blockerUsesToughness() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player2 has Brontodon + Goblin Piker as blocker
        addReadyCreature(player2, new BelligerentBrontodon());
        Permanent blocker = addReadyCreature(player2, new GoblinPiker());

        // Player1 attacks with Grizzly Bears (2/2)
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Goblin Piker (2/1) blocks Grizzly Bears (2/2)
        // Piker deals 1 damage (toughness via Brontodon) → Bears survives (2 toughness, 1 damage)
        // Bears deals 2 damage → Piker dies (1 toughness, 2 damage)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Effect disappears when Brontodon leaves =====

    @Test
    @DisplayName("Effect disappears when Brontodon is removed from battlefield")
    void effectDisappearsWhenBrontodonRemoved() {
        Permanent brontodon = addReadyCreature(player1, new BelligerentBrontodon());
        Permanent spider = addReadyCreature(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4); // toughness

        gd.playerBattlefields.get(player1.getId()).remove(brontodon);

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2); // back to power
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
