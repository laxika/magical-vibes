package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlarewielderTest extends BaseCardTest {

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
    }

    @Test
    @DisplayName("Hardcast: ETB makes both target creatures unable to block; Glarewielder stays")
    void hardcastTwoCreaturesCantBlock() {
        Permanent bear1 = addBears(player2);
        Permanent bear2 = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(bear1.getId(), bear2.getId()));
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bear1.isCantBlockThisTurn()).isTrue();
        assertThat(bear2.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glarewielder"));
    }

    @Test
    @DisplayName("Evoke: one target can't block and Glarewielder is sacrificed as it enters")
    void evokeSacrificesSelfAndCantBlock() {
        Permanent bear = addBears(player2);
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, bear.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger (can't block + evoke sacrifice)

        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glarewielder"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glarewielder"));
    }

    @Test
    @DisplayName("Can enter with zero targets (up to two)")
    void canEnterWithNoTargets() {
        harness.setHand(player1, List.of(new Glarewielder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.<UUID>of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glarewielder"));
    }
}
