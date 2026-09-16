package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrophobia.class, AirElemental.class, GrizzlyBears.class})
class PyrophobiaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the target creature")
    void dealsThreeDamageToTargetCreature() {
        Permanent target = addReadyCreature(player2, new AirElemental());
        castPyrophobia(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cowards can't block this turn, but other creatures can")
    void cowardsCantBlockThisTurn() {
        Permanent target = addReadyCreature(player2, new AirElemental());
        Permanent coward = addReadyCreature(player2, new GrizzlyBears());
        coward.setTransientCreatureTypeOverride(CardSubtype.COWARD);
        Permanent nonCoward = addReadyCreature(player2, new GrizzlyBears());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());

        castPyrophobia(target);
        attacker.setAttacking(true);

        assertThat(bls.canBlockAttacker(gd, coward, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, nonCoward, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castPyrophobia(Permanent target) {
        harness.setHand(player1, List.of(new Pyrophobia()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
