package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeSpeakerZeganaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters and draws one card when it is the only creature")
    void entersAloneDrawsOne() {
        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with counters equal to the greatest power among other creatures you control")
    void entersWithCountersFromOtherCreatures() {
        addCreatureReady(player1, new HillGiant());

        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(drawnCards(player1)).isEqualTo(4);
    }

    @Test
    @DisplayName("Ignores creatures controlled by the opponent")
    void ignoresOpponentCreatures() {
        addCreatureReady(player2, new AvatarOfMight());

        castZegana();

        Permanent zegana = findPermanent(player1, "Prime Speaker Zegana");
        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    private void castZegana() {
        harness.setHand(player1, List.of(new PrimeSpeakerZegana()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int drawnCards(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }
}
