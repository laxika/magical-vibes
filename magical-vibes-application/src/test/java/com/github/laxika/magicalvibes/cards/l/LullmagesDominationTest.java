package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LullmagesDomination.class, GrizzlyBears.class})
class LullmagesDominationTest extends BaseCardTest {

    @Test
    void reducesCostWhenTargetControllerHasEightCardsInGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player2, graveyardWithCards(8));
        harness.setHand(player1, List.of(new LullmagesDomination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceCostWhenTargetControllerHasFewerThanEightCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player2, graveyardWithCards(7));
        harness.setHand(player1, List.of(new LullmagesDomination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checksTheTargetControllersGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, graveyardWithCards(8));
        harness.setHand(player1, List.of(new LullmagesDomination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
