package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WuWarship.class, Island.class, Forest.class})
class WuWarshipTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts Wu Warship onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new WuWarship(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof WuWarship);
    }

    @Test
    @DisplayName("Wu Warship can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());
        addCreatureReady(player1, new WuWarship());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new WuWarship());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when only its controller controls an Island")
    void cannotAttackWhenOnlyControllerControlsIsland() {
        addCreatureReady(player1, new WuWarship());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wu Warship cannot attack when defending player controls a non-Island land")
    void cannotAttackWhenDefenderControlsNonIslandLand() {
        harness.addToBattlefield(player2, new Forest());
        addCreatureReady(player1, new WuWarship());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
