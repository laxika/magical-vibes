package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorShield.class, GrizzlyBears.class, TyphoidRats.class})
class MirrorShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2 and hexproof")
    void equippedCreatureGetsBoostAndHexproof() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature destroys a deathtouch creature blocking it")
    void equippedCreatureBlocksDeathtouchCreature() {
        Permanent attacker = addReady(player1, new TyphoidRats());
        attacker.setAttacking(true);

        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent shield = addReady(player2, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        declareBlock(attacker, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Typhoid Rats");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipped creature destroys a deathtouch creature blocking it when it attacks")
    void deathtouchCreatureBlocksEquippedCreature() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent shield = addReady(player1, new MirrorShield());
        shield.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addReady(player2, new TyphoidRats());

        declareBlock(creature, blocker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature without deathtouch does not trigger Mirror Shield")
    void nonDeathtouchCreatureDoesNotTrigger() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent shield = addReady(player2, new MirrorShield());
        shield.setAttachedTo(creature.getId());

        declareBlock(attacker, creature);

        assertThat(gd.stack)
                .noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Mirror Shield"));
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
