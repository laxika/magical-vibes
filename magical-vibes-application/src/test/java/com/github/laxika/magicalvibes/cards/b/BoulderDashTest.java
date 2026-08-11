package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoulderDashTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToFirstTargetAndOneDamageToSecondTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player2.getId());
        harness.castSorcery(player1, 0, List.of(battlefield.get(0).getId(), battlefield.get(1).getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void dealsTwoDamageToFirstTargetAndOneDamageToSecondPlayerTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(creatureTarget, player2.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotCastWithDuplicateTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoulderDash()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

}
