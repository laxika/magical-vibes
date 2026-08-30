package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChillbringerTest extends BaseCardTest {

    @Test
    void tapsAnOpponentsCreatureAndSkipsItsNextUntap() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        castChillbringer(opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void cannotTargetAControllerCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Chillbringer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownCreature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castChillbringer(UUID targetId) {
        harness.setHand(player1, List.of(new Chillbringer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }

}
