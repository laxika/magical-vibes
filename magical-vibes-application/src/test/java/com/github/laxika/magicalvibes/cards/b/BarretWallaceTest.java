package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarretWallace.class, GrizzlyBears.class, SwiftfootBoots.class})
class BarretWallaceTest extends BaseCardTest {

    @Test
    void dealsCombatDamagePlusNoTriggerDamageWithoutEquippedCreatures() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        setLifeTotals(20, 20);

        attackWith(barret);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void dealsDamageForEachEquippedCreatureYouControl() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        attachEquipment(player1, barret);
        attachEquipment(player1, equippedCreature);
        attachEquipment(player1, equippedCreature);
        attachEquipment(player2, opponentCreature);
        setLifeTotals(20, 20);

        attackWith(barret);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void attackWith(Permanent creature) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
    }

    private void attachEquipment(Player player, Permanent creature) {
        Permanent equipment = new Permanent(new SwiftfootBoots());
        equipment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        equipment.setAttachedTo(creature.getId());
    }

    private void setLifeTotals(int player1Life, int player2Life) {
        harness.setLife(player1, player1Life);
        harness.setLife(player2, player2Life);
    }
}
