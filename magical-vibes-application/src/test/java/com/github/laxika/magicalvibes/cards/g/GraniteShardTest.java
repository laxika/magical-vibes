package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraniteShardTest extends BaseCardTest {

    @Test
    @DisplayName("The generic activation deals 1 damage to a target player")
    void genericActivationDealsDamageToTargetPlayer() {
        Permanent shard = addReadyShard();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(shard.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The red activation deals 1 damage to a target creature")
    void redActivationDealsDamageToTargetCreature() {
        Permanent shard = addReadyShard();
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(shard.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    private Permanent addReadyShard() {
        Permanent shard = new Permanent(new GraniteShard());
        shard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(shard);
        return shard;
    }
}
