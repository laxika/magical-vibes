package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcticMerfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker without returning a creature")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent merfolk = findMerfolk(player1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kicker returns a creature and puts a +1/+1 counter on Arctic Merfolk")
    void castWithKicker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreatureWithPermanent(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent merfolk = findMerfolk(player1);
        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot kick without returning a creature")
    void cannotKickWithoutReturningCreature() {
        harness.setHand(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("return");
    }

    @Test
    @DisplayName("Cannot kick by returning an opponent's creature")
    void cannotReturnOpponentsCreatureForKicker() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    private Permanent findMerfolk(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Arctic Merfolk"))
                .findFirst()
                .orElseThrow();
    }
}
