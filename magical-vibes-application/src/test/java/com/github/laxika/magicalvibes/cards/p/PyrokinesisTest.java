package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PyrokinesisTest extends BaseCardTest {

    private void addFullMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Divides 4 damage as chosen among target creatures")
    void dividesFourDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        harness.castInstant(player1, 0, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can be cast by exiling a red card from hand instead of paying mana")
    void castByExilingRedCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrokinesis(), new Shock()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, Map.of(bears.getId(), 4), 1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Shock");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-red card")
    void alternateCostRequiresRedCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrokinesis(), new GrizzlyBears()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateExileFromHand(player1, 0, Map.of(bears.getId(), 4), 1)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Players can't be assigned damage")
    void cannotTargetPlayers() {
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(player2.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to 4")
    void assignmentsMustSumToFour() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrokinesis()));
        addFullMana();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }
}
