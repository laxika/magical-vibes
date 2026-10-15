package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodArmor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlotCrawler.class, Forest.class, GrizzlyBears.class, GiantGrowth.class, UnholyStrength.class, BlanchwoodArmor.class})
class WoodlotCrawlerTest extends BaseCardTest {

    private Permanent attackWith(WoodlotCrawler card) {
        Permanent perm = addCreatureReady(player1, card);
        perm.setAttacking(true);
        return perm;
    }

    @Test
    @DisplayName("Cannot be blocked when defending player controls a Forest")
    void forestwalkStopsBlock() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = attackWith(new WoodlotCrawler());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Green creature cannot block it (protection from green)")
    void protectionStopsGreenBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = attackWith(new WoodlotCrawler());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        Permanent crawler = addCreatureReady(player1, new WoodlotCrawler());

        // A legal target so the spell itself is playable.
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, crawler.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by a black spell")
    void canBeTargetedByBlackSpell() {
        Permanent crawler = addCreatureReady(player1, new WoodlotCrawler());

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, crawler.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void forestwalkStopsNonGreenBlocker() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new WoodlotCrawler());
        Permanent attacker = attackWith(new WoodlotCrawler());
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void attackingPlayersForestDoesNotPreventNonGreenBlock() {
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = attackWith(new WoodlotCrawler());
        Permanent blocker = addCreatureReady(player2, new WoodlotCrawler());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void preventsGreenCombatDamageWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent crawler = addCreatureReady(player2, new WoodlotCrawler());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        resolveCombat();

        harness.assertOnBattlefield(player2, "Woodlot Crawler");
        assertThat(crawler.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void greenAuraCannotRemainAttached() {
        Permanent crawler = addCreatureReady(player1, new WoodlotCrawler());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BlanchwoodArmor());
        aura.setAttachedTo(crawler.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Woodlot Crawler");
        harness.assertNotOnBattlefield(player1, "Blanchwood Armor");
        harness.assertInGraveyard(player1, "Blanchwood Armor");
    }
}
