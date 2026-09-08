package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PollenRemedyTest extends BaseCardTest {

    @Test
    void preventsThreeDamageDividedAmongCreatureAndPlayer() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void kickedPreventsSixDamageAndSacrificesALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, Map.of(bears.getId(), 3, player2.getId(), 3),
                List.of(), List.of(), false, land.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        harness.assertInGraveyard(player1, "Plains");
    }
}
