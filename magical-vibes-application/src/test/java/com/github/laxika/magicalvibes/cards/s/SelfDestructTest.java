package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelfDestruct.class, GrizzlyBears.class, HillGiant.class, WallOfSwords.class})
class SelfDestructTest extends BaseCardTest {

    @Test
    @DisplayName("The creature deals its power to another target and to itself")
    void dealsPowerDamageToOtherTargetAndItself() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.castInstant(player1, 0, List.of(sourceId, targetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The second target may be a player")
    void dealsPowerDamageToPlayerAndItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(sourceId, player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The second target must be different from the source creature")
    void cannotTargetSourceCreatureAsOtherTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SelfDestruct()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, sourceId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }
}
