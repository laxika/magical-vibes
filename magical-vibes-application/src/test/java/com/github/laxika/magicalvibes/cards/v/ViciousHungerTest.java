package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ViciousHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2, and controller gains 2 life")
    void dealsDamageAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A tougher creature survives with 2 marked damage; controller still gains 2 life")
    void tougherTargetSurvivesButLifeStillGained() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, bear);
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
