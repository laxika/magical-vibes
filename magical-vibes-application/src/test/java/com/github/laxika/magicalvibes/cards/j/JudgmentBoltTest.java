package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudgmentBolt.class, EchoCirclet.class, GrizzlyBears.class, ColossalDreadmaw.class})
class JudgmentBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a creature and damage equal to controlled Equipment to its controller")
    void dealsDamageToCreatureAndItsController() {
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts only Equipment controlled by the spell's controller")
    void countsOnlyControllersEquipment() {
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new EchoCirclet());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Colossal Dreadmaw"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Counts Equipment at resolution")
    void countsEquipmentAtResolution() {
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        var equipmentId = harness.getPermanentId(player1, "Echo Circlet");
        var targetId = harness.getPermanentId(player2, "Colossal Dreadmaw");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(equipmentId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player1, new EchoCirclet());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
