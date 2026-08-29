package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InnerStruggleTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a 2/2 when it deals 2 damage to itself")
    void killsCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InnerStruggle()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves a 3/5 alive with 3 marked damage")
    void survivesWhenPowerIsBelowToughness() {
        harness.addToBattlefield(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new InnerStruggle()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Wall of Swords");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new InnerStruggle()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, plainsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InnerStruggle()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Inner Struggle");
    }
}
