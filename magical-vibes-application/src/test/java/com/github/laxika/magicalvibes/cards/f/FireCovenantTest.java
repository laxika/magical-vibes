package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class FireCovenantTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new FireCovenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Pays X life and divides X damage among target creatures")
    void dividesDamageAmongCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        harness.castInstantForX(player1, 0, 4, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can't be cast for more life than you have")
    void cannotPayMoreLifeThanYouHave() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();
        harness.getGameData().playerLifeTotals.put(player1.getId(), 3);

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 4, Map.of(bears.getId(), 4))
        ).isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Players can't be assigned damage")
    void cannotTargetPlayers() {
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(player2.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepare();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 3, Map.of(bears.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }
}
