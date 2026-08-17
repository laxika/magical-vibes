package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HedronFieldPuristsTest extends BaseCardTest {

    @Test
    @DisplayName("Level 1 prevents one damage from each source to its controller and creatures they control")
    void levelOnePreventsOneDamageToControllerAndCreature() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        levelUp(player1, purists);

        castShock(player2, player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Level 5 prevents two damage from each source")
    void levelFivePreventsTwoDamage() {
        Permanent purists = addCreatureReady(player1, new HedronFieldPurists());
        for (int i = 0; i < 5; i++) {
            levelUp(player1, purists);
        }

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(purists.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Hedron-Field Purists does not prevent damage to an opponent's creature")
    void doesNotPreventDamageToOpponentsCreature() {
        addCreatureReady(player1, new HedronFieldPurists());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        levelUp(player1, findPermanent(player1, "Hedron-Field Purists"));

        castShock(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    private void levelUp(Player player, Permanent purists) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(purists);
        harness.activateAbility(player, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void castShock(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }
}
