package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheFallOfKroogTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the chosen opponent's land and damages that player and their creatures")
    void resolvesAgainstChosenOpponent() {
        harness.setLife(player2, 20);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires the land to be controlled by the chosen opponent")
    void rejectsLandControlledByAnotherPlayer() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player2.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires targeting an opponent")
    void rejectsControllerAsOpponentTarget() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TheFallOfKroog()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player1.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
