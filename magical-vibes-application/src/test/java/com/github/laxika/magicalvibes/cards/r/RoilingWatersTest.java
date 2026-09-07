package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilingWaters.class, GrizzlyBears.class})
class RoilingWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two opponent creatures and makes the target player draw two cards")
    void returnsCreaturesAndDrawsCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0, List.of(player2.getId(), first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 4);
    }

    @Test
    @DisplayName("Allows returning no creatures while still drawing two cards")
    void allowsNoCreatureTargets() {
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player2.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }
}
