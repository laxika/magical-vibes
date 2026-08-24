package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({PowerstoneMinefield.class, GrizzlyBears.class, HillGiant.class})
class PowerstoneMinefieldTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature")
    void damagesAttackingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addReadyCreature(player2, new HillGiant());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage to each blocking creature")
    void damagesBlockingCreature() {
        harness.addToBattlefield(player1, new PowerstoneMinefield());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
