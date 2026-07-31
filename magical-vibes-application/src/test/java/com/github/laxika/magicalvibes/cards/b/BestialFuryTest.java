package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BestialFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield schedules a draw at the beginning of the next turn's upkeep")
    void entering_schedulesDrawAtNextUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BestialFury()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked, it gets +4/+0 and gains trample")
    void becomesBlocked_boostsAndGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(4);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(attacker.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addBestialFuryAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.inMutationScope(attacker::resetModifiers);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("No trigger when the Aura is on the battlefield but not attached")
    void notAttached_noTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addBestialFury(player1);
        attacker.setAttacking(true);

        addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Bestial Fury"));
    }

    private Permanent addBestialFury(Player player) {
        Permanent perm = new Permanent(new BestialFury());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBestialFuryAttachedTo(Player player, Permanent creature) {
        Permanent perm = addBestialFury(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
