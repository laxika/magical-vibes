package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorBoomerangTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature deals 1 damage and Razor Boomerang returns to its owner's hand")
    void dealsDamageAndReturnsToHand() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = addBoomerangReady(player1);
        boomerang.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(boomerang.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(boomerang);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("damage from Razor Boomerang"));
    }

    @Test
    @DisplayName("Razor Boomerang stays unattached when the damage target becomes illegal")
    void illegalTargetStopsReturnToHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent boomerang = addBoomerangReady(player1);
        boomerang.setAttachedTo(creature.getId());

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boomerang);
        assertThat(boomerang.getAttachedTo()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(boomerang.getCard());
    }

    private Permanent addBoomerangReady(Player player) {
        Permanent permanent = new Permanent(new RazorBoomerang());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
