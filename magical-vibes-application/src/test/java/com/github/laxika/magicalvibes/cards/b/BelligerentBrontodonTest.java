package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentBrontodon.class, GoblinPiker.class, GiantSpider.class, GrizzlyBears.class,
        TurnToFrog.class})
class BelligerentBrontodonTest extends BaseCardTest {

    @Test
    @DisplayName("Removing Brontodon's abilities stops the toughness damage effect")
    void losingSourceAbilityStopsEffect() {
        Permanent brontodon = addCreatureReady(player1, new BelligerentBrontodon());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, brontodon.getId());
        spider.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature that loses its own abilities still uses toughness while Brontodon is present")
    void losingAffectedCreatureAbilitiesDoesNotStopEffect() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, spider.getId());
        spider.setCounterCount(CounterType.PLUS_ZERO_PLUS_ONE, 2);
        spider.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Combat damage uses modified toughness without subtracting marked damage")
    void usesCurrentToughnessDespiteMarkedDamage() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        spider.setMarkedDamage(3);
        spider.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Brontodon (4/6) deals 6 combat damage (its toughness)")
    void brontodonUsesToughnessForOwnDamage() {
        Permanent brontodon = addCreatureReady(player1, new BelligerentBrontodon());

        assertThat(gqs.getEffectiveCombatDamage(gd, brontodon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Brontodon attacking unblocked deals toughness damage to opponent")
    void brontodonUnblockedDealsToughnessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent brontodon = addCreatureReady(player1, new BelligerentBrontodon());
        brontodon.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14); // 20 - 6
    }

    @Test
    @DisplayName("Giant Spider (2/4) with Brontodon deals 4 combat damage")
    void creatureWithHigherToughnessUsesToughness() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Goblin Piker (2/1) with Brontodon deals 1 combat damage (toughness, not power)")
    void creatureWithHigherPowerStillUsesToughness() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Piker (2/1) unblocked deals 1 damage with Brontodon")
    void highPowerCreatureDealsReducedDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        piker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // 20 - 1
    }

    @Test
    @DisplayName("Grizzly Bears (2/2) with Brontodon deals 2 combat damage (unchanged)")
    void creatureWithEqualPowerToughnessUnchanged() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creatures are not affected by Brontodon")
    void opponentCreaturesNotAffected() {
        addCreatureReady(player1, new BelligerentBrontodon());
        Permanent opponentPiker = addCreatureReady(player2, new GoblinPiker());

        // Opponent's Goblin Piker (2/1) still deals 2 (its power)
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentPiker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controlled blocker uses toughness for combat damage")
    void blockerUsesToughness() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player2 has Brontodon + Goblin Piker as blocker
        addCreatureReady(player2, new BelligerentBrontodon());
        Permanent blocker = addCreatureReady(player2, new GoblinPiker());

        // Player1 attacks with Grizzly Bears (2/2)
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Goblin Piker (2/1) blocks Grizzly Bears (2/2)
        // Piker deals 1 damage (toughness via Brontodon) → Bears survives (2 toughness, 1 damage)
        // Bears deals 2 damage → Piker dies (1 toughness, 2 damage)
        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Effect disappears when Brontodon is removed from battlefield")
    void effectDisappearsWhenBrontodonRemoved() {
        Permanent brontodon = addCreatureReady(player1, new BelligerentBrontodon());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(4); // toughness

        gd.playerBattlefields.get(player1.getId()).remove(brontodon);

        assertThat(gqs.getEffectiveCombatDamage(gd, spider)).isEqualTo(2); // back to power
    }

}
