package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormSeeker.class, BarbaryApes.class})
class StormSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Storm Seeker deals damage equal to target player's hand size")
    void dealsDamageEqualToHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.setHand(player2, List.of(new BarbaryApes(), new BarbaryApes(), new BarbaryApes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Storm Seeker uses target player's hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.setHand(player2, List.of(new BarbaryApes(), new BarbaryApes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new BarbaryApes());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Storm Seeker deals 0 damage if target player has no cards in hand")
    void dealsZeroDamageWithEmptyHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new StormSeeker()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Storm Seeker can target its controller")
    void canTargetController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StormSeeker(), new BarbaryApes(), new BarbaryApes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Storm Seeker cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        harness.setHand(player1, List.of(new StormSeeker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
