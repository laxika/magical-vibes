package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BallistaChargerTest extends BaseCardTest {

    @Test
    void crewingAnimatesBallistaChargerAndAttackTriggerDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent charger = addBallistaChargerReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void attackTriggerCanDealDamageToTargetCreature() {
        addBallistaChargerReady(player1);
        addCreatureReady(player1, new SerraAngel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addBallistaChargerReady(Player player) {
        Permanent charger = new Permanent(new BallistaCharger());
        charger.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(charger);
        return charger;
    }
}
