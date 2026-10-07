package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GraniteGrip;
import com.github.laxika.magicalvibes.cards.w.WarlordsAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreefolkMystic.class, GiantCockroach.class, GraniteGrip.class, TreacherousLink.class,
        WarlordsAxe.class})
class TreefolkMysticTest extends BaseCardTest {

    @Test
    @DisplayName("When Treefolk Mystic blocks, it destroys all Auras attached to the attacker")
    void blocksDestroysAllAurasAttachedToAttacker() {
        addReadyMystic(player2);
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);
        addAttachedAura(player1, new GraniteGrip(), attacker);
        addAttachedAura(player1, new TreacherousLink(), attacker);
        addAttachedEquipment(player1, attacker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Granite Grip");
        harness.assertNotOnBattlefield(player1, "Treacherous Link");
        harness.assertInGraveyard(player1, "Granite Grip");
        harness.assertInGraveyard(player1, "Treacherous Link");
        harness.assertOnBattlefield(player1, "Warlord's Axe");
    }

    @Test
    @DisplayName("When Treefolk Mystic blocks, it destroys Auras attached to the attacker regardless of Aura controller")
    void blocksDestroysAurasAttachedToAttackerRegardlessOfAuraController() {
        addReadyMystic(player2);
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);
        Permanent targetedAura = addAttachedAura(player1, new GraniteGrip(), attacker);
        Permanent opponentControlledAura = addAttachedAura(player2, new TreacherousLink(), attacker);
        Permanent otherCreature = addCreatureReady(player1, new GiantCockroach());
        Permanent unrelatedAura = addAttachedAura(player1, new TreacherousLink(), otherCreature);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(targetedAura.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentControlledAura.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(unrelatedAura.getId()));
        harness.assertInGraveyard(player1, "Granite Grip");
        harness.assertInGraveyard(player2, "Treacherous Link");
    }

    @Test
    @DisplayName("When Treefolk Mystic becomes blocked, it destroys all Auras attached to the blocker")
    void becomesBlockedDestroysAllAurasAttachedToBlocker() {
        Permanent mystic = addReadyMystic(player1);
        mystic.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantCockroach());
        addAttachedAura(player2, new GraniteGrip(), blocker);
        addAttachedAura(player2, new TreacherousLink(), blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Granite Grip");
        harness.assertNotOnBattlefield(player2, "Treacherous Link");
        harness.assertInGraveyard(player2, "Granite Grip");
        harness.assertInGraveyard(player2, "Treacherous Link");
    }

    @Test
    @DisplayName("When Treefolk Mystic becomes blocked by multiple creatures, it destroys Auras attached to each blocker")
    void becomesBlockedByMultipleCreaturesDestroysAurasAttachedToEachBlocker() {
        Permanent mystic = addReadyMystic(player1);
        mystic.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GiantCockroach());
        Permanent secondBlocker = addCreatureReady(player2, new GiantCockroach());
        addAttachedAura(player2, new GraniteGrip(), firstBlocker);
        addAttachedAura(player2, new TreacherousLink(), secondBlocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Granite Grip");
        harness.assertNotOnBattlefield(player2, "Treacherous Link");
        harness.assertInGraveyard(player2, "Granite Grip");
        harness.assertInGraveyard(player2, "Treacherous Link");
    }

    @Test
    @DisplayName("Blocking destroys opposing Auras but leaves Auras on Treefolk Mystic intact")
    void blockingLeavesMysticsOwnAurasIntact() {
        Permanent mystic = addReadyMystic(player2);
        Permanent attacker = addCreatureReady(player1, new GiantCockroach());
        attacker.setAttacking(true);
        addAttachedAura(player1, new GraniteGrip(), attacker);
        addAttachedAura(player2, new TreacherousLink(), mystic);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Granite Grip");
        harness.assertNotOnBattlefield(player1, "Granite Grip");
        harness.assertOnBattlefield(player2, "Treacherous Link");
        harness.assertNotInGraveyard(player2, "Treacherous Link");
    }

    @Test
    @DisplayName("The trigger destroys Auras attached at resolution, even if there were none when it triggered")
    void checksAttachedAurasAtResolution() {
        Permanent mystic = addReadyMystic(player1);
        mystic.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantCockroach());
        Permanent otherCreature = addCreatureReady(player2, new GiantCockroach());
        Permanent aura = addAttachedAura(player2, new GraniteGrip(), otherCreature);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        aura.setAttachedTo(blocker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Granite Grip");
        harness.assertInGraveyard(player2, "Granite Grip");
    }

    private Permanent addReadyMystic(Player player) {
        return addCreatureReady(player, new TreefolkMystic());
    }

    private Permanent addAttachedAura(Player player,
                                      Card auraCard,
                                      Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void addAttachedEquipment(Player player, Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new WarlordsAxe());
        equipment.setAttachedTo(creature.getId());
    }
}
