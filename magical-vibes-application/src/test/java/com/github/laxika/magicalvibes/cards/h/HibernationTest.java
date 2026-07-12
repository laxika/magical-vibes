package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HibernationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all green permanents to their owners' hands, regardless of controller")
    void returnsAllGreenPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName()).contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return nongreen permanents")
    void doesNotReturnNongreenPermanents() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Resolves with no green permanents in play")
    void resolvesWithNoGreenPermanents() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Hibernation()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }
}
