package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingForces.class, WeiInfantry.class, ManorGargoyle.class, Swamp.class})
class OverwhelmingForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures the target opponent controls and draws one card per destroyed")
    void destroysOpponentCreaturesAndDraws() {
        Permanent theirs1 = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        Permanent theirs2 = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new WeiInfantry());

        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(theirs1.getId()) || p.getId().equals(theirs2.getId()));
        // Controller's own creature is untouched
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(mine.getId()));
        // One card drawn per creature destroyed
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws no cards when the target opponent controls no creatures")
    void drawsNoCardsWithNoCreatures() {
        harness.addToBattlefield(player1, new WeiInfantry());

        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Leaves noncreature permanents controlled by the target opponent untouched")
    void leavesNoncreaturePermanentsUntouched() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());

        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Indestructible creatures are not destroyed and do not count toward cards drawn")
    void indestructibleNotDestroyedNotCounted() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        // Manor Gargoyle has defender, so its static ability makes it indestructible.
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());

        harness.setHand(player1, List.of(new OverwhelmingForces()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(gargoyle.getId()));
        // Only the destroyed Wei Infantry counts: 1 card drawn, not 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
