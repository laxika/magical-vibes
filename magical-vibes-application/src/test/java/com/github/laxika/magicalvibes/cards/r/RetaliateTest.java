package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RetaliateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys each creature that dealt damage to you this turn")
    void destroysCreaturesThatDealtDamageToYou() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent damagedCreature = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent undamagedCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player2, indexOf(player2, damagedCreature), null, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Retaliate()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(undamagedCreature)
                .doesNotContain(damagedCreature);
        harness.assertInGraveyard(player2, "Prodigal Sorcerer");
    }

    @Test
    @DisplayName("Does nothing when no creature dealt damage to you this turn")
    void doesNothingWhenNoCreatureDealtDamage() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Retaliate()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
