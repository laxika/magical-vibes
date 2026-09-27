package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MineCollapse.class, GrizzlyBears.class, Mountain.class, MuYanlingSkyDancer.class})
class MineCollapseTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MineCollapse()));
        addMineCollapseMana();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new MineCollapse()));
        addMineCollapseMana();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Alternate cost sacrifices a Mountain on your turn")
    void castsBySacrificingMountain() {
        harness.forceActivePlayer(player1);
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MineCollapse()));

        harness.castInstantWithAlternateCost(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"), List.of(mountain));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Alternate cost is unavailable during an opponent's turn")
    void alternateCostRequiresYourTurn() {
        harness.forceActivePlayer(player2);
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MineCollapse()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears"), List.of(mountain)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new MineCollapse()));
        addMineCollapseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMineCollapseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
